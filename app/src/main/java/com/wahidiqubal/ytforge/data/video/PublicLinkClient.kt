package com.wahidiqubal.ytforge.data.video

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

object PublicLinkClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun upload(file: File): String = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("reqtype", "fileupload")
            .addFormDataPart(
                "fileToUpload",
                file.name,
                file.asRequestBody("video/mp4".toMediaType()),
            )
            .build()
        val request = Request.Builder()
            .url("https://catbox.moe/user/api.php")
            .header("User-Agent", "YTForge")
            .post(body)
            .build()
        http.newCall(request).execute().use { response ->
            val text = response.body?.string()?.trim().orEmpty()
            if (!response.isSuccessful || !text.startsWith("https://")) {
                error("Public link failed (HTTP ${response.code}). ${text.take(160)}")
            }
            text
        }
    }
}
