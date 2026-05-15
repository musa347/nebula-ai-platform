package com.aiagent.plugin.client

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class McpClient {

    private val mcpUrl = "http://localhost:8081"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mapper = ObjectMapper()
    private val jsonMediaType = "application/json".toMediaType()

    fun search(query: String): List<String> {
        val json = mapper.writeValueAsString(mapOf("query" to query))
        val request = Request.Builder()
            .url("$mcpUrl/api/v1/repo/search")
            .post(json.toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val node = mapper.readTree(body)
            return node.path("files").map { it.asText() }.toList()
        }
    }

    fun grep(query: String): List<String> {
        val json = mapper.writeValueAsString(mapOf("query" to query))
        val request = Request.Builder()
            .url("$mcpUrl/api/v1/repo/grep")
            .post(json.toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val node = mapper.readTree(body)
            return node.path("matches").map { it.path("file").asText() }.toList().distinct()
        }
    }
}
