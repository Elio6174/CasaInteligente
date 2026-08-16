package com.example.ei_c_jorgesalinas_joelbailon

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class Conexion(
    private val serverUrl: String,
    private val onConectado: () -> Unit,
    private val onDesconectado: () -> Unit,
    private val onMensaje: (String) -> Unit,
    private val onError: ((Throwable) -> Unit)? = null
) {
    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    fun conectar() {
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "Conectado al servidor: $serverUrl")
                onConectado()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMensaje(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // No usamos mensajes binarios en este proyecto
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                Log.i(TAG, "Cerrando conexión: $reason")
                onDesconectado()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "Conexión cerrada: $reason")
                onDesconectado()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Falla en la conexión", t)
                onDesconectado()
                onError?.invoke(t)
            }
        })
    }

    /** Envía un mensaje de texto al servidor (ej. comandos como encender/apagar). */
    fun enviarMensaje(mensaje: String): Boolean {
        return webSocket?.send(mensaje) ?: false
    }

    /** Cierra la conexión de forma ordenada. Llamar en onStop() u onDestroy(). */
    fun desconectar() {
        webSocket?.close(1000, "Cliente cerró la conexión")
    }

    companion object {
        private const val TAG = "Conexion"
    }
}