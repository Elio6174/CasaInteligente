package com.example.ei_c_jorgesalinas_joelbailon

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

private const val TINACO_WS_URL = "ws://jorgeeliodor.com:5000/ws/tinaco"

private val COLOR_CONECTADO = Color.parseColor("#4CAF50")
private val COLOR_DESCONECTADO = Color.parseColor("#D03F3F")

data class EstadoTinaco(
    val nivel: Double,
    val bombaEncendida: Boolean,
    val puedeEncender: Boolean,
    val puedeApagar: Boolean,
    val espConectado: Boolean,
    val timestamp: String
)

class Tinaco : AppCompatActivity() {

    private lateinit var labelNivelAgua: TextView
    private lateinit var textView3: TextView
    private lateinit var radioButton: RadioButton
    private lateinit var radioButtonSensor: RadioButton
    private lateinit var botonEncender: Button
    private lateinit var botonApagar: Button

    private lateinit var conexion: Conexion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tinaco)

        val regresar = findViewById<ImageButton>(R.id.imageButton)
        labelNivelAgua = findViewById(R.id.labelNivelAgua)
        textView3 = findViewById(R.id.textView3)
        radioButton = findViewById(R.id.estadoServidor)
        radioButtonSensor = findViewById(R.id.estadoSensor)
        botonEncender = findViewById(R.id.button4)
        botonApagar = findViewById(R.id.button5)

        setIndicadorConexion(conectado = false)
        setIndicadorSensor(conectado = false)

        regresar.setOnClickListener {
            try {
                conexion.desconectar()
                startActivity(Intent(this, MainActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                Log.e("ERROR_MAIN", "Error", e)
            }
        }

        conexion = Conexion(
            serverUrl = TINACO_WS_URL,
            onConectado = {
                runOnUiThread { setIndicadorConexion(conectado = true) }
            },
            onDesconectado = {
                runOnUiThread { setIndicadorConexion(conectado = false) }
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
                        labelNivelAgua.text = "${estado.nivel}%"
                        textView3.text = if (estado.bombaEncendida)
                            "Estado de la bomba: Encendida"
                        else
                            "Estado de la bomba: Apagada"
                        botonEncender.isEnabled = estado.puedeEncender
                        botonApagar.isEnabled = estado.puedeApagar
                        setIndicadorSensor(conectado = estado.espConectado)
                    }
                } catch (e: Exception) {
                    Log.e("TINACO", "Error al parsear mensaje", e)
                }
            },
            onError = { t ->
                runOnUiThread {
                    Log.e("TINACO", "Error de conexión: ${t.message}")
                }
            }
        )

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