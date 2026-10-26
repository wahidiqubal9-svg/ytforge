package com.wahidiqubal.ytforge.data.settings

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Serializable
data class ApiSettings(
    val providerId: String = "deepseek",
    val baseUrl: String = "https://api.deepseek.com/v1",
    val model: String = "deepseek-chat",
    val apiKey: String = "",
    val sarvamKey: String = "",
    val youtubeClientId: String = "",
    val githubToken: String = "",
    val renderRepo: String = "",
    val renderBranch: String = "main",
    val renderToken: String = "",
    val githubLogin: String = "",
    val githubOAuthClientId: String = "",
    val videoRatio: String = "9:16",
    val videoQuality: String = "720p30",
    val previewFirst: Boolean = true,
    val previewQuality: String = "360p",
    val maxDurationSec: Int = 60,
)

data class ProviderPreset(
    val id: String,
    val name: String,
    val baseUrl: String,
    val model: String,
    val kind: ProviderKind,
    val hint: String,
    val supportsVision: Boolean = false,
)

enum class ProviderKind { OPENAI, ANTHROPIC }

object Providers {
    val all = listOf(
        ProviderPreset(
            id = "deepseek",
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com/v1",
            model = "deepseek-chat",
            kind = ProviderKind.OPENAI,
            hint = "OpenAI-compatible",
            supportsVision = false,
        ),
        ProviderPreset(
            id = "openai",
            name = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            model = "gpt-4o-mini",
            kind = ProviderKind.OPENAI,
            hint = "ChatGPT models",
            supportsVision = true,
        ),
        ProviderPreset(
            id = "claude",
            name = "Claude",
            baseUrl = "https://api.anthropic.com",
            model = "claude-3-5-sonnet-latest",
            kind = ProviderKind.ANTHROPIC,
            hint = "Anthropic Messages API",
            supportsVision = true,
        ),
        ProviderPreset(
            id = "openrouter",
            name = "OpenRouter",
            baseUrl = "https://openrouter.ai/api/v1",
            model = "openai/gpt-4o-mini",
            kind = ProviderKind.OPENAI,
            hint = "Any model via one key",
            supportsVision = true,
        ),
        ProviderPreset(
            id = "gemini",
            name = "Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/",
            model = "gemini-2.5-flash",
            kind = ProviderKind.OPENAI,
            hint = "OpenAI-compatible endpoint",
            supportsVision = true,
        ),
        ProviderPreset(
            id = "custom",
            name = "Custom",
            baseUrl = "",
            model = "",
            kind = ProviderKind.OPENAI,
            hint = "Any OpenAI-compatible API",
            supportsVision = false,
        ),
    )

    fun byId(id: String): ProviderPreset = all.firstOrNull { it.id == id } ?: all.first()
}

class SecureSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ytforge_secure", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun load(): ApiSettings {
        val encrypted = prefs.getString(KEY_SETTINGS, null) ?: return ApiSettings()
        return runCatching {
            json.decodeFromString<ApiSettings>(decrypt(encrypted))
        }.getOrDefault(ApiSettings())
    }

    fun save(settings: ApiSettings) {
        prefs.edit().putString(KEY_SETTINGS, encrypt(json.encodeToString(ApiSettings.serializer(), settings))).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
        }
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val bytes = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun decrypt(payload: String): String {
        val (ivPart, dataPart) = payload.split(":", limit = 2)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(128, Base64.decode(ivPart, Base64.NO_WRAP)),
        )
        return cipher.doFinal(Base64.decode(dataPart, Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "ytforge_api_settings_v1"
        private const val KEY_SETTINGS = "settings_blob"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
