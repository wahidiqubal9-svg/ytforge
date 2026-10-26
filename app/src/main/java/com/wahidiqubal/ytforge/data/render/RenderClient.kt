package com.wahidiqubal.ytforge.data.render

import android.content.Context
import android.util.Base64
import com.wahidiqubal.ytforge.data.github.GitHubHttp
import com.wahidiqubal.ytforge.data.github.executeGitHub
import com.wahidiqubal.ytforge.data.settings.ApiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

data class RenderOutput(
    val video: File,
    val preview: File?,
    val thumbnail: File?,
)

class RenderClient(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = GitHubHttp.client().newBuilder()
        .followRedirects(true)
        .build()

    suspend fun render(
        settings: ApiSettings,
        projectZip: ByteArray,
        jobId: String,
        onProgress: (String) -> Unit = {},
    ): RenderOutput = withContext(Dispatchers.IO) {
        val repo = settings.renderRepo.trim().trim('/')
        val token = settings.renderToken.trim()
        require(repo.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) {
            "Set a render repo in Settings as owner/repo."
        }
        require(token.isNotBlank()) { "Add the render GitHub token in Settings." }
        val branch = settings.renderBranch.ifBlank { "main" }

        onProgress("Uploading project…")
        upload(repo, token, branch, jobId, projectZip)
        onProgress("Starting renderer…")
        dispatch(repo, token, branch, jobId)

        onProgress("Waiting for a GitHub runner…")
        val runId = waitForRun(repo, token, jobId, onProgress)
        onProgress("Downloading render…")
        val artifactUrl = findArtifact(repo, token, runId, jobId)
        val zip = downloadArtifact(artifactUrl, token)

        val outDir = File(context.filesDir, "renders/$jobId").apply { mkdirs() }
        val files = extract(zip, outDir)
        val video = files["video-720p.mp4"] ?: error("Render finished without video-720p.mp4")
        onProgress("Render ready.")
        RenderOutput(
            video = video,
            preview = files["preview-360p.mp4"],
            thumbnail = files["thumbnail.jpg"],
        )
    }

    private fun upload(repo: String, token: String, branch: String, jobId: String, zip: ByteArray) {
        val payload = buildJsonObject {
            put("message", "render $jobId")
            put("branch", branch)
            put("content", Base64.encodeToString(zip, Base64.NO_WRAP))
        }
        val request = Request.Builder()
            .url("$API/repos/$repo/contents/jobs/$jobId/project.zip")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "YTForge")
            .put(payload.toString().toRequestBody(JSON))
            .build()
        http.executeGitHub(request).use { response ->
            if (!response.isSuccessful) error(githubError("Upload", response))
        }
    }

    private fun dispatch(repo: String, token: String, branch: String, jobId: String) {
        val payload = buildJsonObject {
            put("ref", branch)
            putJsonObject("inputs") {
                put("job_id", jobId)
            }
        }
        val request = Request.Builder()
            .url("$API/repos/$repo/actions/workflows/render.yml/dispatches")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "YTForge")
            .post(payload.toString().toRequestBody(JSON))
            .build()
        http.executeGitHub(request).use { response ->
            if (!response.isSuccessful) error(githubError("Dispatch", response))
        }
    }

    private fun waitForRun(
        repo: String,
        token: String,
        jobId: String,
        onProgress: (String) -> Unit,
    ): Long {
        val expected = "ytforge-render-$jobId"
        var runId: Long? = null
        repeat(180) { attempt ->
            if (runId == null) {
                val request = Request.Builder()
                    .url("$API/repos/$repo/actions/runs?event=workflow_dispatch&per_page=30")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "YTForge")
                    .get()
                    .build()
                http.executeGitHub(request).use { response ->
                    if (!response.isSuccessful) error(githubError("Runs", response))
                    val body = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
                    val runs = body["workflow_runs"]?.jsonArray ?: return@use
                    runId = runs.asSequence()
                        .map { it.jsonObject }
                        .firstOrNull { it["display_title"]?.jsonPrimitive?.content == expected }
                        ?.get("id")?.jsonPrimitive?.content?.toLongOrNull()
                }
                if (runId != null) onProgress("Runner started…")
            }

            runId?.let { id ->
                val request = Request.Builder()
                    .url("$API/repos/$repo/actions/runs/$id")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "YTForge")
                    .get()
                    .build()
                http.executeGitHub(request).use { response ->
                    if (!response.isSuccessful) error(githubError("Run status", response))
                    val run = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
                    val status = run["status"]?.jsonPrimitive?.content.orEmpty()
                    val conclusion = run["conclusion"]?.jsonPrimitive?.content
                    if (status == "completed") {
                        if (conclusion == "success") return id
                        error("Render failed on GitHub Actions (${conclusion ?: "unknown"}). Open the run: $API/repos/$repo/actions/runs/$id")
                    }
                    onProgress(
                        when (status) {
                            "queued" -> "Queued…"
                            "in_progress" -> "Rendering…"
                            else -> status.ifBlank { "Waiting…" }
                        },
                    )
                }
            }
            Thread.sleep(if (attempt < 6) 3_000 else 5_000)
        }
        error("Render timed out. Check GitHub Actions for the run.")
    }

    private fun findArtifact(repo: String, token: String, runId: Long, jobId: String): String {
        repeat(20) {
            val request = Request.Builder()
                .url("$API/repos/$repo/actions/runs/$runId/artifacts")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "YTForge")
                .get()
                .build()
            http.executeGitHub(request).use { response ->
                if (!response.isSuccessful) error(githubError("Artifacts", response))
                val body = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
                val artifact = body["artifacts"]?.jsonArray
                    ?.map { it.jsonObject }
                    ?.firstOrNull { it["name"]?.jsonPrimitive?.content == "render-$jobId" }
                val url = artifact?.get("archive_download_url")?.jsonPrimitive?.content
                if (url != null) return url
            }
            Thread.sleep(2_000)
        }
        error("Render completed but no artifact was found.")
    }

    private fun downloadArtifact(url: String, token: String): ByteArray {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "YTForge")
            .get()
            .build()
        http.executeGitHub(request).use { response ->
            if (!response.isSuccessful) error(githubError("Download", response))
            return response.body?.bytes() ?: error("Empty render artifact.")
        }
    }

    private fun extract(zipBytes: ByteArray, outDir: File): Map<String, File> {
        val files = mutableMapOf<String, File>()
        ZipInputStream(zipBytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name.substringAfterLast('/')
                if (entry.isDirectory || name.isBlank()) continue
                val target = File(outDir, name)
                target.outputStream().use { output -> zip.copyTo(output) }
                files[name] = target
                zip.closeEntry()
            }
        }
        return files
    }

    private fun githubError(operation: String, response: Response): String {
        val body = runCatching { response.body?.string().orEmpty() }.getOrDefault("")
        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.content
        }.getOrNull()
        return "$operation failed (HTTP ${response.code}): ${message ?: body.take(300)}"
    }

    private fun copyStream(input: InputStream, output: java.io.OutputStream) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            output.write(buffer, 0, read)
        }
    }

    companion object {
        private const val API = "https://api.github.com"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
