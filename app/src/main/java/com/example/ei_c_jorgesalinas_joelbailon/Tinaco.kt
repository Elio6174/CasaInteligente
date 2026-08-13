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

// URL real de tu servidor (WebSocket sobre HTTP normal, puerto 5000)
private const val TINACO_WS_URL = "ws://jorgeeliodor.com:5000/ws/tinaco"

private val COLOR_CONECTADO = Color.parseColor("#4CAF50")    // verde
private val COLOR_DESCONECTADO = Color.parseColor("#D03F3F") // rojo

class Tinaco : AppCompatActivity() {

    private lateinit var labelNivelAgua: TextView
    private lateinit var textView3: TextView   // "Estado de la bomba" - reutilizamos para mostrar ON/OFF
    private lateinit var radioButton: RadioButton
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
        radioButton = findViewById(R.id.radioButton)
        botonEncender = findViewById(R.id.button4)
        botonApagar = findViewById(R.id.button5)

        // Estado inicial: rojo (desconectado) hasta que se confirme la conexión
        setIndicadorConexion(conectado = false)

        regresar.setOnClickListener {
            try {
                startActivity(Intent(this, MainActivity::class.java))
                finish() // cierra Tinaco para que dispare onStop() y no se quede viva en el back stack
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                Log.e("ERROR_MAIN", "Error", e)
            }
        }

        // Creamos el objeto Conexion una sola vez; conectar/desconectar
        // se controla en onStart()/onStop() según la visibilidad de la pantalla.
        conexion = Conexion(
            serverUrl = TINACO_WS_URL,
            onConectado = {
                runOnUiThread { setIndicadorConexion(conectado = true) }
            },
            onDesconectado = {
                runOnUiThread { setIndicadorConexion(conectado = false) }
            },
            onEstadoActualizado = { estado ->
                runOnUiThread {
                    labelNivelAgua.text = "${estado.nivel}%"
                    textView3.text = if (estado.bombaEncendida)
                        "Estado de la bomba: Encendida"
                    else
                        "Estado de la bomba: Apagada"

                    // Deshabilita "Encender" si el tinaco ya está lleno,
                    // y "Apagar" si está casi vacío (evita comandos sin sentido)
                    botonEncender.isEnabled = estado.puedeEncender
                    botonApagar.isEnabled = estado.puedeApagar
                }
            },
            onError = { t ->
                runOnUiThread {
                    Log.e("TINACO", "Error de conexión: ${t.message}")
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        // Se conecta cada vez que la pantalla se vuelve visible
        // (primera vez, o al regresar desde otra Activity/segundo plano)
        conexion.conectar()

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

    override fun onStop() {
        super.onStop()
        // Se desconecta en cuanto la pantalla deja de ser visible
        // (al navegar a otra Activity, presionar Home, etc.)
        conexion.desconectar()
        setIndicadorConexion(conectado = false)
    }

    private fun setIndicadorConexion(conectado: Boolean) {
        val color = if (conectado) COLOR_CONECTADO else COLOR_DESCONECTADO
        radioButton.buttonTintList = ColorStateList.valueOf(color)
        radioButton.isChecked = conectado
    }
}