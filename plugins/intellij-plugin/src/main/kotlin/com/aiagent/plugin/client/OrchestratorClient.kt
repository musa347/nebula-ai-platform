package com.aiagent.plugin.client

import com.intellij.openapi.components.Service
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

@Service
class OrchestratorClient {

    var orchestratorUrl: String = "ws://localhost:8082/ws/stream-response"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // no timeout for streaming
        .build()

    private var webSocket: WebSocket? = null
    private var connected = false

    fun connect(
        onEvent: (String) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder().url(orchestratorUrl).build()
        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) { connected = true }

            override fun onMessage(ws: WebSocket, text: String) {
                when {
                    text.contains("\"type\":\"DONE\"") -> { connected = false; onDone() }
                    text.contains("\"error\"")         -> onError(text)
                    else                               -> onEvent(text)
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) { connected = false; onDone() }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                connected = false
                onError(t.message ?: "Connection failed")
            }
        })
    }

    fun submitTask(task: String, maxRetries: Int = 3) {
        webSocket?.send("""{"task":"${task.replace("\"", "\\\"")}","maxRetries":$maxRetries}""")
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        connected = false
    }

    fun isConnected(): Boolean = connected
}
