package com.example.ei_c_jorgesalinas_joelbailon

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Modelo de una lectura recibida del servidor.
 * Coincide con el JSON que manda el servidor:
 *   {"nivel": 63.5, "bomba_encendida": true, "timestamp": "2026-08-13T10:00:00"}
 */
data class EstadoTinaco(
    val nivel: Double,
    val bombaEncendida: Boolean,
    val puedeEncender: Boolean,
    val puedeApagar: Boolean,
    val timestamp: String
)

/**
 * Clase encargada exclusivamente de manejar la conexión WebSocket
 * con el servidor. No conoce nada de la UI: solo notifica cambios
 * a través de callbacks para que la Activity decida qué hacer.
 *
 * Uso:
 *   val conexion = Conexion(
 *       serverUrl = "ws://TU_IP:8000/ws/tinaco",
 *       onConectado = { ... },
 *       onDesconectado = { ... },
 *       onNivelActualizado = { nivel -> ... }
 *   )
 *   conexion.conectar()
 *   ...
 *   conexion.desconectar() // en onDestroy()
 */
class Conexion(
    private val serverUrl: String,
    private val onConectado: () -> Unit,
    private val onDesconectado: () -> Unit,
    private val onEstadoActualizado: (EstadoTinaco) -> Unit,
    private val onError: ((Throwable) -> Unit)? = null
) {
    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // sin timeout, la conexión debe quedar abierta
        .pingInterval(20, TimeUnit.SECONDS)     // mantiene la conexión viva
        .build()

    /** Inicia la conexión con el servidor. */
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
                try {
                    val json = JSONObject(text)
                    val estado = EstadoTinaco(
                        nivel = json.getDouble("nivel"),
                        bombaEncendida = json.getBoolean("bomba_encendida"),
                        puedeEncender = json.getBoolean("puede_encender"),
                        puedeApagar = json.getBoolean("puede_apagar"),
                        timestamp = json.getString("timestamp")
                    )
                    onEstadoActualizado(estado)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al parsear mensaje", e)
                }
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

    /** Envía un mensaje de texto al servidor (ej. para encender/apagar la bomba). */
    fun enviarMensaje(mensaje: String): Boolean {
        return webSocket?.send(mensaje) ?: false
    }

    /** Cierra la conexión de forma ordenada. Llamar en onDestroy(). */
    fun desconectar() {
        webSocket?.close(1000, "Cliente cerró la conexión")
    }

    companion object {
        private const val TAG = "Conexion"
    }
}