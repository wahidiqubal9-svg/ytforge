package com.wahidiqubal.ytforge.data.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class GitHubDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresIn: Int,
    val interval: Int,
) {
    val verificationUrl: String
        get() = "$verificationUri?user_code=" + URLEncoder.encode(userCode, "UTF-8")
}

data class GitHubToken(
    val accessToken: String,
    val scope: String,
)

class GitHubDeviceAuth {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun start(clientId: String, scopes: String = "repo workflow read:user"): GitHubDeviceCode =
        withContext(Dispatchers.IO) {
            require(clientId.isNotBlank()) { "GitHub OAuth Client ID is missing." }
            val request = Request.Builder()
                .url("https://github.com/login/device/code")
                .header("Accept", "application/json")
                .post(
                    FormBody.Builder()
                        .add("client_id", clientId)
                        .add("scope", scopes)
                        .build(),
                )
                .build()
            http.executeGitHub(request).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("GitHub device login failed (HTTP ${response.code}).")
                val root = json.parseToJsonElement(body).jsonObject
                val deviceCode = root["device_code"]?.jsonPrimitive?.content
                    ?: error("GitHub returned no device code: ${body.take(200)}")
                val userCode = root["user_code"]?.jsonPrimitive?.content ?: error("GitHub returned no user code.")
                val verificationUri = root["verification_uri"]?.jsonPrimitive?.content
                    ?: "https://github.com/login/device"
                GitHubDeviceCode(
                    deviceCode = deviceCode,
                    userCode = userCode,
                    verificationUri = verificationUri,
                    expiresIn = root["expires_in"]?.jsonPrimitive?.content?.toIntOrNull() ?: 900,
                    interval = root["interval"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5,
                )
            }
        }

    suspend fun poll(
        clientId: String,
        code: GitHubDeviceCode,
        onStatus: (String) -> Unit = {},
    ): GitHubToken = withContext(Dispatchers.IO) {
        var interval = code.interval.coerceAtLeast(5).toLong()
        val deadline = System.currentTimeMillis() + code.expiresIn * 1000L
        onStatus("Waiting for GitHub authorization…")
        while (System.currentTimeMillis() < deadline) {
            delay(interval * 1000L)
            val request = Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .post(
                    FormBody.Builder()
                        .add("client_id", clientId)
                        .add("device_code", code.deviceCode)
                        .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                        .build(),
                )
                .build()
            http.executeGitHub(request).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("GitHub token request failed (HTTP ${response.code}).")
                val root = json.parseToJsonElement(body).jsonObject
                val token = root["access_token"]?.jsonPrimitive?.content
                if (!token.isNullOrBlank()) {
                    val scope = root["scope"]?.jsonPrimitive?.content.orEmpty()
                    if (!scope.contains("repo") || !scope.contains("workflow")) {
                        error("GitHub did not grant repo + workflow permissions (got: $scope).")
                    }
                    return@withContext GitHubToken(token, scope)
                }
                when (val errorCode = root["error"]?.jsonPrimitive?.content) {
                    "authorization_pending" -> Unit
                    "slow_down" -> {
                        interval += 5
                        onStatus("GitHub asked to slow down…")
                    }
                    "expired_token" -> error("GitHub login code expired. Start again.")
                    "access_denied" -> error("GitHub authorization was cancelled.")
                    "device_flow_disabled" -> error("Enable Device Flow on the GitHub OAuth App.")
                    else -> {
                        val description = root["error_description"]?.jsonPrimitive?.content
                        error(description ?: "GitHub login failed: ${errorCode ?: body.take(200)}")
                    }
                }
            }
        }
        error("GitHub login timed out. Start again.")
    }
}
