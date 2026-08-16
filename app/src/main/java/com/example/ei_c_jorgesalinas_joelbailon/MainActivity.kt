package com.example.ei_c_jorgesalinas_joelbailon

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val botonEntrar = findViewById<MaterialButton>(R.id.botonEntrar)
        botonEntrar.setOnClickListener {
            startActivity(Intent(this, CasaInteligente::class.java))
        }
    }
}