package com.wahidiqubal.ytforge.data.github

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class GitHubSetupResult(
    val owner: String,
    val repo: String,
    val branch: String,
    val login: String,
) {
    val fullName: String get() = "$owner/$repo"
}

class GitHubSetupClient(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun setup(token: String, onStatus: (String) -> Unit = {}): GitHubSetupResult =
        withContext(Dispatchers.IO) {
            onStatus("Reading GitHub account…")
            val login = getLogin(token)
            val repo = "ytforge-render"
            onStatus("Checking $login/$repo…")
            val existing = getRepo(token, login, repo)
            if (existing == null) {
                onStatus("Creating private $login/$repo…")
                createRepo(token, repo)
            }
            val branch = getRepo(token, login, repo)?.get("default_branch")?.jsonPrimitive?.content
                ?: "main"

            onStatus("Installing render workflow…")
            val workflow = context.assets.open("render.yml").bufferedReader().use { it.readText() }
            putWorkflow(token, login, repo, branch, workflow)

            onStatus("Enabling Actions…")
            runCatching { enableActions(token, login, repo) }

            onStatus("Verifying render workflow…")
            verifyWorkflow(token, login, repo, branch)
            onStatus("GitHub setup complete.")
            GitHubSetupResult(login, repo, branch, login)
        }

    private fun getLogin(token: String): String {
        val request = api("https://api.github.com/user", token).get().build()
        http.executeGitHub(request).use { response ->
            if (!response.isSuccessful) error(githubError("Account", response))
            val body = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
            return body["login"]?.jsonPrimitive?.content ?: error("GitHub returned no login.")
        }
    }

    private fun getRepo(token: String, owner: String, repo: String): kotlinx.serialization.json.JsonObject? {
        val request = api("$API/repos/$owner/$repo", token).get().build()
        http.executeGitHub(request).use { response ->
            if (response.code == 404) return null
            if (!response.isSuccessful) error(githubError("Repository check", response))
            return json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
        }
    }

    private fun createRepo(token: String, repo: String) {
        val payload = buildJsonObject {
            put("name", repo)
            put("private", true)
            put("auto_init", true)
            put("description", "Private HyperFrames render backend for YTForge")
        }
        val request = api("$API/user/repos", token)
            .post(payload.toString().toRequestBody(JSON))
            .build()
        http.executeGitHub(request).use { response ->
            if (response.code == 422) return
            if (!response.isSuccessful) error(githubError("Create repository", response))
        }
    }

    private fun putWorkflow(token: String, owner: String, repo: String, branch: String, content: String) {
        val path = ".github/workflows/render.yml"
        var sha: String? = null
        val getRequest = api("$API/repos/$owner/$repo/contents/$path?ref=${encode(branch)}", token).get().build()
        http.newCall(getRequest).execute().use { response ->
            if (response.isSuccessful) {
                sha = runCatching {
                    json.parseToJsonElement(response.body?.string().orEmpty())
                        .jsonObject["sha"]?.jsonPrimitive?.content
                }.getOrNull()
            } else if (response.code != 404) {
                error(githubError("Workflow read", response))
            }
        }
        val payload = buildJsonObject {
            put("message", "Configure YTForge render backend")
            put("content", Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
            put("branch", branch)
            if (sha != null) put("sha", sha)
        }
        val request = api("$API/repos/$owner/$repo/contents/$path", token)
            .put(payload.toString().toRequestBody(JSON))
            .build()
        http.executeGitHub(request).use { response ->
            if (!response.isSuccessful) error(githubError("Workflow upload", response))
        }
    }

    private fun enableActions(token: String, owner: String, repo: String) {
        val payload = buildJsonObject {
            put("enabled", true)
            put("allowed_actions", "all")
        }
        val request = api("$API/repos/$owner/$repo/actions/permissions", token)
            .put(payload.toString().toRequestBody(JSON))
            .build()
        http.executeGitHub(request).use { response ->
            if (response.code == 403 || response.code == 404) return
            if (!response.isSuccessful) error(githubError("Enable Actions", response))
        }
    }

    private fun verifyWorkflow(token: String, owner: String, repo: String, branch: String) {
        repeat(5) { attempt ->
            val request = api(
                "$API/repos/$owner/$repo/actions/workflows/render.yml?ref=${encode(branch)}",
                token,
            ).get().build()
            http.executeGitHub(request).use { response ->
                if (response.isSuccessful) return
                if (response.code != 404) error(githubError("Workflow verify", response))
            }
            if (attempt < 4) Thread.sleep(1500)
        }
        error("render.yml is not visible on branch $branch yet. Open the repo and check Actions.")
    }

    private fun api(url: String, token: String): Request.Builder =
        Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "YTForge")

    private fun githubError(operation: String, response: Response): String {
        val body = runCatching { response.body?.string().orEmpty() }.getOrDefault("")
        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.content
        }.getOrNull()
        return "$operation failed (HTTP ${response.code}): ${message ?: body.take(250)}"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    companion object {
        private const val API = "https://api.github.com"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
