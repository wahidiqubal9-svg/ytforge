package com.wahidiqubal.ytforge.data.github

import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.net.InetAddress
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * GitHub DNS lookups can fail on mobile networks. Try the system resolver first, then
 * DNS-over-HTTPS through Cloudflare and Google (both bootstrapped by IP so they do not
 * depend on the broken system resolver).
 */
object GitHubHttp {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val dohBootstrap = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .dns { hostname ->
            when (hostname) {
                "cloudflare-dns.com" -> listOf(InetAddress.getByName("1.1.1.1"))
                "dns.google" -> listOf(InetAddress.getByName("8.8.8.8"))
                else -> Dns.SYSTEM.lookup(hostname)
            }
        }
        .build()

    private class CacheEntry(val addresses: List<InetAddress>, val expiresAt: Long)

    private val fallbackDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            runCatching { Dns.SYSTEM.lookup(hostname) }
                .getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?.let { return it }

            cache[hostname]?.takeIf { it.expiresAt > System.currentTimeMillis() }?.let {
                return it.addresses
            }

            val addresses = resolveWithDoH(hostname)
            if (addresses.isEmpty()) throw UnknownHostException("Unable to resolve host \"$hostname\"")
            cache[hostname] = CacheEntry(addresses, System.currentTimeMillis() + 5 * 60_000L)
            return addresses
        }
    }

    fun client(): OkHttpClient = OkHttpClient.Builder()
        .dns(fallbackDns)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun resolveWithDoH(hostname: String): List<InetAddress> {
        val endpoints = listOf(
            "https://cloudflare-dns.com/dns-query?name=${encode(hostname)}&type=A",
            "https://dns.google/resolve?name=${encode(hostname)}&type=A",
        )
        for (endpoint in endpoints) {
            val addresses = runCatching { queryDoH(endpoint) }.getOrDefault(emptyList())
            if (addresses.isNotEmpty()) return addresses
        }
        return emptyList()
    }

    private fun queryDoH(url: String): List<InetAddress> {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/dns-json")
            .get()
            .build()
        dohBootstrap.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val root = JSONObject(response.body?.string().orEmpty())
            val answers = root.optJSONArray("Answer") ?: return emptyList()
            val result = mutableListOf<InetAddress>()
            for (index in 0 until answers.length()) {
                val answer = answers.optJSONObject(index) ?: continue
                if (answer.optInt("type") != 1) continue
                val data = answer.optString("data")
                if (data.isBlank()) continue
                runCatching { InetAddress.getByName(data) }.getOrNull()?.let(result::add)
            }
            return result
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}

/**
 * Retry only transient DNS failures. HTTP and auth errors must pass through.
 */
internal fun OkHttpClient.executeGitHub(request: Request, attempts: Int = 6): Response {
    var last: UnknownHostException? = null
    repeat(attempts) { attempt ->
        try {
            return newCall(request).execute()
        } catch (error: UnknownHostException) {
            last = error
            if (attempt < attempts - 1) {
                val wait = when (attempt) {
                    0 -> 700L
                    1 -> 1_200L
                    2 -> 2_000L
                    3 -> 3_000L
                    else -> 5_000L
                }
                Thread.sleep(wait)
            }
        }
    }
    throw last ?: IllegalStateException("GitHub network request failed")
}
