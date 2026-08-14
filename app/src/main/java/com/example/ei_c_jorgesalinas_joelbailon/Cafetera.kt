package com.example.ei_c_jorgesalinas_joelbailon

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

private const val CAFETERA_WS_URL = "ws://jorgeeliodor.com:5000/ws/cafetera"

private val COLOR_CONECTADO = Color.parseColor("#4CAF50")    // verde
private val COLOR_DESCONECTADO = Color.parseColor("#D03F3F") // rojo

data class EstadoCafetera(
    val encendida: Boolean,
    val espConectado: Boolean,
    val timestamp: String
)

class Cafetera : AppCompatActivity() {

    private lateinit var radioButton: RadioButton
    private lateinit var radioButtonSensor: RadioButton
    private lateinit var botonEncender: Button
    private lateinit var botonApagar: Button

    private lateinit var conexion: Conexion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cafetera)

        val regresar = findViewById<ImageButton>(R.id.imageButton)
        radioButton = findViewById(R.id.estadoServidor)
        radioButtonSensor = findViewById(R.id.estadoSensor)
        botonEncender = findViewById(R.id.botonEncenderCafetera)
        botonApagar = findViewById(R.id.botonApagarCafetera)

        setIndicadorConexion(conectado = false)
        setIndicadorSensor(conectado = false)

        conexion = Conexion(
            serverUrl = CAFETERA_WS_URL,
            onConectado = {
                runOnUiThread { setIndicadorConexion(conectado = true) }
            },
            onDesconectado = {
                runOnUiThread { setIndicadorConexion(conectado = false) }
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
                        botonEncender.isEnabled = !estado.encendida
                        botonApagar.isEnabled = estado.encendida
                        setIndicadorSensor(conectado = estado.espConectado)
                    }
                } catch (e: Exception) {
                    Log.e("CAFETERA", "Error al parsear mensaje", e)
                }
            },
            onError = { t ->
                runOnUiThread {
                    Toast.makeText(this, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        regresar.setOnClickListener {
            try {
                conexion.desconectar()
                startActivity(Intent(this, MainActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                Log.e("ERROR_MAIN", "Error", e)
            }
        }

        botonEncender.setOnClickListener {
            val enviado = conexion.enviarMensaje("""{"comando":"encender"}""")
            if (!enviado) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }

        botonApagar.setOnClickListener {
            val enviado = conexion.enviarMensaje("""{"comando":"apagar"}""")
            if (!enviado) {
                Toast.makeText(this, "No se pudo enviar: sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        conexion.conectar()
    }

    override fun onStop() {
        super.onStop()
        conexion.desconectar()
        setIndicadorConexion(conectado = false)
        setIndicadorSensor(conectado = false)
    }

    private fun setIndicadorConexion(conectado: Boolean) {
        val color = if (conectado) COLOR_CONECTADO else COLOR_DESCONECTADO
        radioButton.buttonTintList = ColorStateList.valueOf(color)
        radioButton.isChecked = conectado
    }

    private fun setIndicadorSensor(conectado: Boolean) {
        val color = if (conectado) COLOR_CONECTADO else COLOR_DESCONECTADO
        radioButtonSensor.buttonTintList = ColorStateList.valueOf(color)
        radioButtonSensor.isChecked = conectado
    }
}