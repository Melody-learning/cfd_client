package com.astralw.core.network.api

import android.util.Log
import com.astralw.core.network.model.WsActionMessage
import com.astralw.core.network.model.WsIncomingMessage
import com.astralw.core.network.model.WsQuoteData
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * WebSocket 行情推送服务
 *
 * 与后端 `ws://<server>/api/v1/market/stream` 对接。
 * - 连接后发送 subscribe 消息订阅品种
 * - 接收 quote 推送，通过 Flow 暴露给 Repository
 * - 断线自动重连（指数退避: 1s → 2s → 4s → ... → 30s）
 * - 重连后自动重新订阅
 */
class MarketWebSocketService(
    private val client: OkHttpClient,
    private val json: Json,
    private val wsUrl: String,
) {

    private val _quoteFlow = MutableSharedFlow<WsQuoteData>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** 实时行情推送流 */
    val quoteFlow: Flow<WsQuoteData> = _quoteFlow.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var subscribedSymbols: List<String> = emptyList()

    /** 是否由用户主动断开（不触发自动重连） */
    @Volatile
    private var userDisconnected: Boolean = false

    /** 当前重连延迟（指数退避） */
    @Volatile
    private var reconnectDelayMs: Long = INITIAL_RECONNECT_DELAY_MS

    @Volatile
    var isConnected: Boolean = false
        private set

    /**
     * 建立 WebSocket 连接
     */
    fun connect() {
        if (webSocket != null) {
            Log.d(TAG, "connect: already has socket, skip")
            return
        }

        userDisconnected = false
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "onOpen: WebSocket connected")
                isConnected = true
                reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS // 重置退避
                // 重连时自动重新订阅
                if (subscribedSymbols.isNotEmpty()) {
                    sendSubscribe(subscribedSymbols)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val msg = json.decodeFromString<WsIncomingMessage>(text)
                    if (msg.type == "quote" && msg.data != null) {
                        _quoteFlow.tryEmit(msg.data)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "onMessage parse error: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosing: $code $reason")
                isConnected = false
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosed: $code $reason")
                isConnected = false
                this@MarketWebSocketService.webSocket = null
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure: ${t.message}")
                isConnected = false
                this@MarketWebSocketService.webSocket = null
                scheduleReconnect()
            }
        })
    }

    /**
     * 订阅品种列表
     */
    fun subscribe(symbols: List<String>) {
        subscribedSymbols = symbols
        if (isConnected) {
            sendSubscribe(symbols)
        }
    }

    /**
     * 断开连接（用户主动，不触发自动重连）
     */
    fun disconnect() {
        userDisconnected = true
        webSocket?.close(NORMAL_CLOSURE, "client disconnect")
        webSocket = null
        isConnected = false
    }

    /**
     * 指数退避重连调度
     *
     * 从 onClosed / onFailure 回调触发。
     * 实际重连由 RemoteMarketRepository 的降级轮询循环驱动
     * （每 2 秒检查 isConnected，断线时调用 connect()）。
     * 此处只负责清理状态和计算下次重连延迟。
     */
    private fun scheduleReconnect() {
        if (userDisconnected) {
            Log.d(TAG, "scheduleReconnect: user disconnected, skip")
            return
        }

        Log.d(TAG, "scheduleReconnect: will reconnect in ${reconnectDelayMs}ms")
        // 指数退避，上限 30 秒
        reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(MAX_RECONNECT_DELAY_MS)
    }

    private fun sendSubscribe(symbols: List<String>) {
        val msg = WsActionMessage(action = "subscribe", symbols = symbols)
        val text = json.encodeToString(WsActionMessage.serializer(), msg)
        val sent = webSocket?.send(text) ?: false
        Log.d(TAG, "subscribe: ${symbols.size} symbols, sent=$sent")
    }

    companion object {
        private const val TAG = "MarketWS"
        private const val NORMAL_CLOSURE = 1000
        private const val INITIAL_RECONNECT_DELAY_MS = 1_000L
        private const val MAX_RECONNECT_DELAY_MS = 30_000L
    }
}
