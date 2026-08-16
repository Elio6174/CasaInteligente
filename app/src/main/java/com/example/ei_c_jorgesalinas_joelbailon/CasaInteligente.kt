package com.example.ei_c_jorgesalinas_joelbailon

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

private const val TINACO_WS_URL = "ws://jorgeeliodor.com:5000/ws/tinaco"
private const val CAFETERA_WS_URL = "ws://jorgeeliodor.com:5000/ws/cafetera"
private const val PORTON_WS_URL = "ws://jorgeeliodor.com:5000/ws/porton"

private val COLOR_CONECTADO = Color.parseColor("#4CAF50")    // verde
private val COLOR_DESCONECTADO = Color.parseColor("#D03F3F") // rojo

data class EstadoTinaco(
    val nivel: Double,
    val bombaEncendida: Boolean,
    val puedeEncender: Boolean,
    val puedeApagar: Boolean,
    val espConectado: Boolean,
    val timestamp: String
)

data class EstadoCafetera(
    val encendida: Boolean,
    val espConectado: Boolean,
    val timestamp: String
)

data class EstadoPorton(
    val abierto: Boolean,
    val espConectado: Boolean,
    val timestamp: String
)

class CasaInteligente : AppCompatActivity() {

    // --- Tinaco ---
    private lateinit var labelNivelAgua: TextView
    private lateinit var progressNivelTinaco: ProgressBar
    private lateinit var textEstadoBomba: TextView
    private lateinit var estadoServidorTinaco: TextView
    private lateinit var estadoSensorTinaco: TextView
    private lateinit var botonEncenderBomba: Button
    private lateinit var botonApagarBomba: Button
    private lateinit var conexionTinaco: Conexion

    // --- Cafetera ---
    private lateinit var textEstadoCafetera: TextView
    private lateinit var estadoServidorCafetera: TextView
    private lateinit var estadoSensorCafetera: TextView
    private lateinit var botonEncenderCafetera: Button
    private lateinit var botonApagarCafetera: Button
    private lateinit var conexionCafetera: Conexion

    // --- Portón ---
    private lateinit var textEstadoPorton: TextView
    private lateinit var estadoServidorPorton: TextView
    private lateinit var estadoSensorPorton: TextView
    private lateinit var botonAbrirPorton: Button
    private lateinit var botonCerrarPorton: Button
    private lateinit var conexionPorton: Conexion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_casa_inteligente)

        val regresar = findViewById<ImageButton>(R.id.imageButton)

        labelNivelAgua = findViewById(R.id.labelNivelAgua)
        progressNivelTinaco = findViewById(R.id.progressNivelTinaco)
        textEstadoBomba = findViewById(R.id.textEstadoBomba)
        estadoServidorTinaco = findViewById(R.id.estadoServidorTinaco)
        estadoSensorTinaco = findViewById(R.id.estadoSensorTinaco)
        botonEncenderBomba = findViewById(R.id.botonEncenderBomba)
        botonApagarBomba = findViewById(R.id.botonApagarBomba)

        textEstadoCafetera = findViewById(R.id.textEstadoCafetera)
        estadoServidorCafetera = findViewById(R.id.estadoServidorCafetera)
        estadoSensorCafetera = findViewById(R.id.estadoSensorCafetera)
        botonEncenderCafetera = findViewById(R.id.botonEncenderCafetera)
        botonApagarCafetera = findViewById(R.id.botonApagarCafetera)

        textEstadoPorton = findViewById(R.id.textEstadoPorton)
        estadoServidorPorton = findViewById(R.id.estadoServidorPorton)
        estadoSensorPorton = findViewById(R.id.estadoSensorPorton)
        botonAbrirPorton = findViewById(R.id.botonAbrirPorton)
        botonCerrarPorton = findViewById(R.id.botonCerrarPorton)

        setIndicador(estadoServidorTinaco, false)
        setIndicador(estadoSensorTinaco, false)
        setIndicador(estadoServidorCafetera, false)
        setIndicador(estadoSensorCafetera, false)
        setIndicador(estadoServidorPorton, false)
        setIndicador(estadoSensorPorton, false)

        regresar.setOnClickListener {
            try {
                conexionTinaco.desconectar()
                conexionCafetera.desconectar()
                conexionPorton.desconectar()
                startActivity(Intent(this, MainActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                Log.e("ERROR_MAIN", "Error", e)
            }
        }

        configurarTinaco()
        configurarCafetera()
        configurarPorton()
    }

    private fun configurarTinaco() {
        conexionTinaco = Conexion(
            serverUrl = TINACO_WS_URL,
            onConectado = { runOnUiThread { setIndicador(estadoServidorTinaco, true) } },
            onDesconectado = {
                runOnUiThread {
                    setIndicador(estadoServidorTinaco, false)
                    setIndicador(estadoSensorTinaco, false)
                }
            },
            onMensaje = { texto ->
                try {
                    val json = JSONObject(texto)
                    val estado = EstadoTinaco(
                        nivel = json.getDouble("nivel"),
                        bombaEncendida = json.getBoolean("bomba_encendida"),
                        puedeEncender = json.getBoolean("puede_encender"),
                        puedeApagar = json.getBoolean("puede_apagar"),
                        espConectado = json.getBoolean("esp_conectado"),
                        timestamp = json.getString("timestamp")
                    )
                    runOnUiThread {
                        labelNivelAgua.text = "Nivel de Agua: ${estado.nivel}%"
                        progressNivelTinaco.progress = estado.nivel.toInt()
                        textEstadoBomba.text = if (estado.bombaEncendida)
                            "Estado: Encendida"
                        else
                            "Estado: Apagada"
                        botonEncenderBomba.isEnabled = estado.puedeEncender
                        botonApagarBomba.isEnabled = estado.puedeApagar
                        setIndicador(estadoSensorTinaco, estado.espConectado)
                    }
                } catch (e: Exception) {
                    Log.e("TINACO", "Error al parsear mensaje", e)
                }
            },
            onError = { t -> Log.e("TINACO", "Error de conexión: ${t.message}") }
        )

        botonEncenderBomba.setOnClickListener {
            if (!conexionTinaco.enviarMensaje("""{"comando":"encender"}""")) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
        botonApagarBomba.setOnClickListener {
            if (!conexionTinaco.enviarMensaje("""{"comando":"apagar"}""")) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarCafetera() {
        conexionCafetera = Conexion(
            serverUrl = CAFETERA_WS_URL,
            onConectado = { runOnUiThread { setIndicador(estadoServidorCafetera, true) } },
            onDesconectado = {
                runOnUiThread {
                    setIndicador(estadoServidorCafetera, false)
                    setIndicador(estadoSensorCafetera, false)
                }
            },
            onMensaje = { texto ->
                try {
                    val json = JSONObject(texto)
                    val estado = EstadoCafetera(
                        encendida = json.getBoolean("encendida"),
                        espConectado = json.getBoolean("esp_conectado"),
                        timestamp = json.getString("timestamp")
                    )
                    runOnUiThread {
                        textEstadoCafetera.text = if (estado.encendida)
                            "Estado: Encendida"
                        else
                            "Estado: Apagada"
                        botonEncenderCafetera.isEnabled = !estado.encendida
                        botonApagarCafetera.isEnabled = estado.encendida
                        setIndicador(estadoSensorCafetera, estado.espConectado)
                    }
                } catch (e: Exception) {
                    Log.e("CAFETERA", "Error al parsear mensaje", e)
                }
            },
            onError = { t -> Log.e("CAFETERA", "Error de conexión: ${t.message}") }
        )

        botonEncenderCafetera.setOnClickListener {
            if (!conexionCafetera.enviarMensaje("""{"comando":"encender"}""")) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
        botonApagarCafetera.setOnClickListener {
            if (!conexionCafetera.enviarMensaje("""{"comando":"apagar"}""")) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarPorton() {
        conexionPorton = Conexion(
            serverUrl = PORTON_WS_URL,
            onConectado = { runOnUiThread { setIndicador(estadoServidorPorton, true) } },
            onDesconectado = {
                runOnUiThread {
                    setIndicador(estadoServidorPorton, false)
                    setIndicador(estadoSensorPorton, false)
                }
            },
            onMensaje = { texto ->
                try {
                    val json = JSONObject(texto)
                    val estado = EstadoPorton(
                        abierto = json.getBoolean("abierto"),
                        espConectado = json.getBoolean("esp_conectado"),
                        timestamp = json.getString("timestamp")
                    )
                    runOnUiThread {
                        textEstadoPorton.text = if (estado.abierto)
                            "Estado: Abierto"
                        else
                            "Estado: Cerrado"
                        botonAbrirPorton.isEnabled = !estado.abierto
                        botonCerrarPorton.isEnabled = estado.abierto
                        setIndicador(estadoSensorPorton, estado.espConectado)
                    }
                } catch (e: Exception) {
                    Log.e("PORTON", "Error al parsear mensaje", e)
                }
            },
            onError = { t -> Log.e("PORTON", "Error de conexión: ${t.message}") }
        )

        botonAbrirPorton.setOnClickListener {
            if (!conexionPorton.enviarMensaje("""{"comando":"abrir"}""")) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
        botonCerrarPorton.setOnClickListener {
            if (!conexionPorton.enviarMensaje("""{"comando":"cerrar"}""")) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        conexionTinaco.conectar()
        conexionCafetera.conectar()
        conexionPorton.conectar()
    }

    override fun onStop() {
        super.onStop()
        conexionTinaco.desconectar()
        conexionCafetera.desconectar()
        conexionPorton.desconectar()
        setIndicador(estadoServidorTinaco, false)
        setIndicador(estadoSensorTinaco, false)
        setIndicador(estadoServidorCafetera, false)
        setIndicador(estadoSensorCafetera, false)
        setIndicador(estadoServidorPorton, false)
        setIndicador(estadoSensorPorton, false)
    }

    private fun setIndicador(punto: TextView, conectado: Boolean) {
        punto.setTextColor(if (conectado) COLOR_CONECTADO else COLOR_DESCONECTADO)
    }
}