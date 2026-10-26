package com.wahidiqubal.ytforge.data.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class VoiceResult(
    val wav: ByteArray,
    val durationMs: Long,
    val model: String,
    val speaker: String,
)

class SarvamTtsClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun synthesize(
        apiKey: String,
        text: String,
        languageCode: String = "hi-IN",
    ): VoiceResult = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Add your Sarvam API key in Settings." }
        val clean = text.trim()
        require(clean.isNotEmpty()) { "Add the narration script." }
        require(clean.length <= 2500) { "Sarvam accepts up to 2,500 characters per request. Shorten the script." }

        runCatching {
            request(apiKey, clean, languageCode, model = "bulbul:v4-flash", speaker = "ritu_hi_edtech")
        }.getOrElse { first ->
            runCatching {
                request(apiKey, clean, languageCode, model = "bulbul:v3", speaker = "ritu")
            }.getOrElse { second ->
                throw IllegalStateException(
                    "Sarvam voice failed. ${first.message ?: "v4-flash unavailable"}; " +
                        "fallback: ${second.message ?: "v3 failed"}",
                )
            }
        }
    }

    private fun request(
        apiKey: String,
        text: String,
        languageCode: String,
        model: String,
        speaker: String,
    ): VoiceResult {
        val payload = buildJsonObject {
            put("text", text)
            put("language_code", languageCode)
            put("speaker", speaker)
            put("model", model)
            put("pace", 1.0)
            put("speech_sample_rate", 22050)
            put("output_audio_codec", "wav")
        }
        val request = Request.Builder()
            .url("https://api.sarvam.ai/text-to-speech")
            .header("api-subscription-key", apiKey)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON))
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    json.parseToJsonElement(body).jsonObject["error"]?.jsonObject
                        ?.get("message")?.jsonPrimitive?.content
                }.getOrNull() ?: body.take(300)
                error("HTTP ${response.code}: $message")
            }
            val audio = json.parseToJsonElement(body).jsonObject["audios"]?.jsonArray
                ?.firstOrNull()?.jsonPrimitive?.content
                ?: error("Sarvam returned no audio.")
            val wav = android.util.Base64.decode(audio, android.util.Base64.NO_WRAP)
            val duration = WavUtils.durationMs(wav)
            require(duration > 0) { "Sarvam returned an unreadable WAV file." }
            return VoiceResult(wav, duration, model, speaker)
        }
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

object WavUtils {
    fun durationMs(wav: ByteArray): Long {
        if (wav.size < 44 || wav.copyOfRange(0, 4).toString(Charsets.US_ASCII) != "RIFF") return 0
        var offset = 12
        var byteRate = 0
        while (offset + 8 <= wav.size) {
            val id = wav.copyOfRange(offset, offset + 4).toString(Charsets.US_ASCII)
            val size = readIntLE(wav, offset + 4)
            val dataStart = offset + 8
            if (id == "fmt " && size >= 16 && dataStart + 12 <= wav.size) {
                byteRate = readIntLE(wav, dataStart + 8)
            } else if (id == "data") {
                val available = (wav.size - dataStart).coerceAtLeast(0)
                val dataSize = if (size <= 0 || size > available) available else size
                if (byteRate > 0) return (dataSize.toLong() * 1000L) / byteRate
            }
            offset = dataStart + size + (size and 1)
        }
        return 0L
    }

    private fun readIntLE(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
}
