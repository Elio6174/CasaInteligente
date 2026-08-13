package com.example.ei_c_jorgesalinas_joelbailon

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

// Cambia esta URL por la IP/dominio real de tu servidor.
// Usa "ws://" si es HTTP normal, o "wss://" si tu servidor usa HTTPS/TLS.
private const val TINACO_WS_URL = "ws://TU_IP_O_DOMINIO:8000/ws/tinaco"

private val COLOR_CONECTADO = Color.parseColor("#4CAF50")   // verde
private val COLOR_DESCONECTADO = Color.parseColor("#D03F3F") // rojo (el que ya tenías)

class Tinaco : AppCompatActivity() {

    private lateinit var labelNivelAgua: TextView
    private lateinit var radioButton: RadioButton

    private lateinit var conexion: Conexion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tinaco)

        val regresar = findViewById<ImageButton>(R.id.imageButton)
        labelNivelAgua = findViewById(R.id.labelNivelAgua)
        radioButton = findViewById(R.id.radioButton)

        // Estado inicial: rojo (desconectado) hasta que se confirme la conexión
        setIndicadorConexion(conectado = false)

        regresar.setOnClickListener {
            try {
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
            onNivelActualizado = { nivel ->
                runOnUiThread { labelNivelAgua.text = "${nivel.nivel}%" }
            },
            onError = { t ->
                runOnUiThread {
                    Log.e("TINACO", "Error de conexión: ${t.message}")
                }
            }
        )
        conexion.conectar()
    }

    private fun setIndicadorConexion(conectado: Boolean) {
        val color = if (conectado) COLOR_CONECTADO else COLOR_DESCONECTADO
        radioButton.buttonTintList = ColorStateList.valueOf(color)
        radioButton.isChecked = conectado
    }

    override fun onDestroy() {
        super.onDestroy()
        conexion.desconectar()
    }
}
