package com.wahidiqubal.ytforge.data.settings

import com.wahidiqubal.ytforge.data.chat.ChatClient
import com.wahidiqubal.ytforge.data.chat.ChatMessage
import com.wahidiqubal.ytforge.data.chat.ChatMode
import com.wahidiqubal.ytforge.data.voice.SarvamTtsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class ConnectionTester {
    private val chat = ChatClient()
    private val sarvam = SarvamTtsClient()
    private val http = com.wahidiqubal.ytforge.data.github.GitHubHttp.client()

    suspend fun testChat(settings: ApiSettings): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val reply = chat.complete(
                settings = settings,
                mode = ChatMode.PLAN,
                messages = listOf(ChatMessage("user", "Reply with exactly: OK", ChatMode.PLAN)),
            )
            if (reply.isBlank()) error("Empty reply") else "Chat OK · ${reply.take(60)}"
        }
    }

    suspend fun testSarvam(settings: ApiSettings): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (settings.sarvamKey.isBlank()) error("Sarvam key is empty")
            val voice = sarvam.synthesize(settings.sarvamKey, "Namaste, YTForge voice test.")
            "Voice OK · ${voice.durationMs / 1000.0}s WAV · ${voice.model}"
        }
    }

    suspend fun testRender(settings: ApiSettings): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val repo = settings.renderRepo.trim().trim('/')
            if (repo.isBlank() || settings.renderToken.isBlank()) error("Render repo or token is empty")
            val repoRequest = Request.Builder()
                .url("https://api.github.com/repos/$repo")
                .header("Authorization", "Bearer ${settings.renderToken}")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "YTForge")
                .get()
                .build()
            http.newCall(repoRequest).execute().use { response ->
                if (!response.isSuccessful) error("Repo check failed (HTTP ${response.code})")
            }
            val workflowRequest = Request.Builder()
                .url("https://api.github.com/repos/$repo/actions/workflows/render.yml")
                .header("Authorization", "Bearer ${settings.renderToken}")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "YTForge")
                .get()
                .build()
            http.newCall(workflowRequest).execute().use { response ->
                if (!response.isSuccessful) error("render.yml not found (HTTP ${response.code})")
            }
            "Render repo OK · render.yml found"
        }
    }

    fun testYouTube(settings: ApiSettings): Result<String> = runCatching {
        if (settings.youtubeClientId.isBlank()) {
            "Not configured · manual Share/YouTube is available"
        } else {
            "Client ID saved · OAuth flow is not wired yet · use Share/YouTube"
        }
    }
}
