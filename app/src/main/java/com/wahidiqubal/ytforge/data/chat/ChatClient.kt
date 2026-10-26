package com.wahidiqubal.ytforge.data.chat

import android.util.Base64
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.settings.ProviderKind
import com.wahidiqubal.ytforge.data.settings.Providers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class ChatAttachment(
    val path: String,
    val mimeType: String = "image/jpeg",
    val name: String = "image.jpg",
)

data class ChatMessage(
    val role: String,
    val content: String,
    val mode: ChatMode = ChatMode.PLAN,
    val attachments: List<ChatAttachment> = emptyList(),
)

enum class ChatMode { PLAN, BUILD }

class ChatClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun complete(
        settings: ApiSettings,
        mode: ChatMode,
        messages: List<ChatMessage>,
    ): String = withContext(Dispatchers.IO) {
        if (settings.apiKey.isBlank()) error("Add an AI provider key in Settings first.")
        val preset = Providers.byId(settings.providerId)
        val base = settings.baseUrl.trim().trimEnd('/')
        if (base.isBlank()) error("Add the provider base URL in Settings.")
        if (settings.model.isBlank()) error("Add a model name in Settings.")

        when (preset.kind) {
            ProviderKind.OPENAI -> openAiCompatible(base, settings, mode, messages, preset.supportsVision)
            ProviderKind.ANTHROPIC -> anthropic(base, settings, mode, messages)
        }
    }

    private fun openAiCompatible(
        base: String,
        settings: ApiSettings,
        mode: ChatMode,
        messages: List<ChatMessage>,
        supportsVision: Boolean,
    ): String {
        val payload = buildJsonObject {
            put("model", settings.model)
            put("max_tokens", 2048)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", systemPrompt(mode, settings))
                })
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", message.role)
                        val images = message.attachments.filter {
                            supportsVision && message.role == "user"
                        }
                        if (images.isNotEmpty()) {
                            putJsonArray("content") {
                                add(buildJsonObject {
                                    put("type", "text")
                                    put("text", message.content.ifBlank { "Use the attached images as video assets." })
                                })
                                images.forEach { image ->
                                    val data = imageBase64(image.path) ?: return@forEach
                                    val mime = image.mimeType.ifBlank { "image/jpeg" }
                                    add(buildJsonObject {
                                        put("type", "image_url")
                                        putJsonObject("image_url") {
                                            put("url", "data:$mime;base64,$data")
                                        }
                                    })
                                }
                            }
                        } else {
                            put("content", message.content.ifBlank { "Use the attached images as video assets." })
                        }
                    })
                }
            }
        }

        val request = Request.Builder()
            .url("$base/chat/completions")
            .header("Authorization", "Bearer ${settings.apiKey}")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("${settings.model} error ${response.code}: ${shortError(body)}")
            }
            val root = json.parseToJsonElement(body).jsonObject
            val content = root["choices"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("message")?.jsonObject?.get("content")
            return content?.jsonPrimitive?.content?.trim().orEmpty()
                .ifBlank { error("The provider returned an empty reply.") }
        }
    }

    private fun anthropic(
        base: String,
        settings: ApiSettings,
        mode: ChatMode,
        messages: List<ChatMessage>,
    ): String {
        val endpoint = if (base.endsWith("/v1")) "$base/messages" else "$base/v1/messages"
        val payload = buildJsonObject {
            put("model", settings.model)
            put("max_tokens", 2048)
            put("system", systemPrompt(mode, settings))
            putJsonArray("messages") {
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", if (message.role == "assistant") "assistant" else "user")
                        val images = message.attachments.filter { message.role == "user" }
                        if (images.isNotEmpty()) {
                            putJsonArray("content") {
                                add(buildJsonObject {
                                    put("type", "text")
                                    put("text", message.content.ifBlank { "Use the attached images as video assets." })
                                })
                                images.forEach { image ->
                                    val data = imageBase64(image.path) ?: return@forEach
                                    add(buildJsonObject {
                                        put("type", "image")
                                        putJsonObject("source") {
                                            put("type", "base64")
                                            put("media_type", image.mimeType.ifBlank { "image/jpeg" })
                                            put("data", data)
                                        }
                                    })
                                }
                            }
                        } else {
                            put("content", message.content.ifBlank { "Use the attached images as video assets." })
                        }
                    })
                }
            }
        }

        val request = Request.Builder()
            .url(endpoint)
            .header("x-api-key", settings.apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("${settings.model} error ${response.code}: ${shortError(body)}")
            }
            val root = json.parseToJsonElement(body).jsonObject
            val text = root["content"]?.jsonArray
                ?.mapNotNull { part -> part.jsonObject["text"]?.jsonPrimitive?.content }
                ?.joinToString("\n")
                ?.trim()
            return text.orEmpty().ifBlank { error("Claude returned an empty reply.") }
        }
    }

    private fun systemPrompt(mode: ChatMode, settings: ApiSettings): String = when (mode) {
        ChatMode.PLAN -> """
            You are YTForge, a director for short YouTube videos.
            You are in PLAN mode. Do not claim to render or publish anything.
            Ask short clarifying questions when needed. Then produce a practical plan:
            - hook and story beats
            - 5-8 short scenes with narration
            - visual direction for HTML/CSS motion graphics
            - suggested thumbnail text
            - title options, description and tags
            Keep replies tight, scannable, and specific. Final voiceover will use Sarvam.

        ${videoDefaults(settings)}
        """.trimIndent()

        ChatMode.BUILD -> """
            You are YTForge, a video builder for short YouTube videos.
            You are in BUILD mode. Give execution-ready output: scene HTML/CSS notes,
            exact narration, timing, thumbnail copy, title, description and tags.
            The app will generate Sarvam voiceover, render a 720p preview, and ask
            before uploading to YouTube. Never claim an upload happened until the app confirms it.

        ${videoDefaults(settings)}
        """.trimIndent()
    }

    private fun videoDefaults(settings: ApiSettings): String {
        val quality = when (settings.videoQuality) {
            "720p60" -> "720p at 60fps"
            else -> "720p at 30fps"
        }
        val ratio = when (settings.videoRatio) {
            "16:9" -> "16:9 landscape"
            "1:1" -> "1:1 square"
            else -> "9:16 vertical Shorts"
        }
        val preview = if (settings.previewFirst) {
            "Yes, first render a ${settings.previewQuality} preview"
        } else {
            "No, render the final directly"
        }
        return "Output defaults: $ratio, $quality, maximum ${settings.maxDurationSec}s. Preview first: $preview."
    }

    private fun imageBase64(path: String): String? =
        runCatching { Base64.encodeToString(java.io.File(path).readBytes(), Base64.NO_WRAP) }.getOrNull()

    private fun shortError(body: String): String {
        if (body.isBlank()) return "empty response"
        return runCatching {
            val element = json.parseToJsonElement(body)
            val message = element.jsonObject["error"]?.let { error ->
                runCatching { error.jsonObject["message"]?.jsonPrimitive?.content }
                    .getOrNull() ?: error.jsonPrimitive.content
            }
            message ?: body
        }.getOrDefault(body).take(300)
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
