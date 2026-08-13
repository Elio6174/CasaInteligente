package com.example.ei_c_jorgesalinas_joelbailon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnTinaco = findViewById<Button>(R.id.btnTinaco)
        val btnCafetera = findViewById<Button>(R.id.btnCafetera)

        btnTinaco.setOnClickListener {
            try {
                startActivity(Intent(this, Tinaco::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                Log.e("ERROR_TINACO", "Error", e)
            }
        }

        btnCafetera.setOnClickListener {
            try {
                startActivity(Intent(this, Cafetera::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                Log.e("ERROR_CAFETERA", "Error", e)
            }
        }
    }
}