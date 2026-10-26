package com.wahidiqubal.ytforge.data.chat

import com.wahidiqubal.ytforge.data.settings.ApiSettings
import com.wahidiqubal.ytforge.data.settings.ProviderKind
import com.wahidiqubal.ytforge.data.settings.Providers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ModelCatalogClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(settings: ApiSettings): List<String> = withContext(Dispatchers.IO) {
        val preset = Providers.byId(settings.providerId)
        val base = settings.baseUrl.trim().trimEnd('/')
        require(settings.apiKey.isNotBlank()) { "Add the API key first." }
        require(base.isNotBlank()) { "Add the base URL first." }

        val request = when (preset.kind) {
            ProviderKind.OPENAI -> Request.Builder()
                .url("$base/models")
                .header("Authorization", "Bearer ${settings.apiKey}")
                .get()
                .build()

            ProviderKind.ANTHROPIC -> Request.Builder()
                .url(if (base.endsWith("/v1")) "$base/models" else "$base/v1/models")
                .header("x-api-key", settings.apiKey)
                .header("anthropic-version", "2023-06-01")
                .get()
                .build()
        }

        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    json.parseToJsonElement(body).jsonObject["error"]
                        ?.jsonObject?.get("message")?.jsonPrimitive?.content
                }.getOrNull()
                error("Could not fetch models (HTTP ${response.code}): ${message ?: body.take(200)}")
            }
            val root = json.parseToJsonElement(body).jsonObject
            val models = root["data"]?.jsonArray
                ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
                ?.filter { it.isNotBlank() }
                ?.distinct()
                ?.sorted()
                .orEmpty()
            if (models.isEmpty()) {
                error("The provider returned no models.")
            }
            models
        }
    }

    fun fallback(providerId: String): List<String> = when (providerId) {
        "gemini" -> listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash")
        "deepseek" -> listOf("deepseek-chat", "deepseek-reasoner")
        "openai" -> listOf("gpt-4o-mini", "gpt-4o")
        else -> emptyList()
    }
}
