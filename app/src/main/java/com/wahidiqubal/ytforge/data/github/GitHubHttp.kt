package com.wahidiqubal.ytforge.data.github

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.UnknownHostException

/**
 * GitHub DNS lookups can fail once on mobile networks and succeed immediately after.
 * Retry only that transient failure; HTTP errors and auth errors must pass through.
 */
internal fun OkHttpClient.executeGitHub(request: Request, attempts: Int = 4): Response {
    var last: UnknownHostException? = null
    repeat(attempts) { attempt ->
        try {
            return newCall(request).execute()
        } catch (error: UnknownHostException) {
            last = error
            if (attempt < attempts - 1) Thread.sleep(700L * (attempt + 1))
        }
    }
    throw last ?: IllegalStateException("GitHub network request failed")
}
