package com.example.sahti

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Inscrire_type : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inscrire_type)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bt_back3 = findViewById<TextView>(R.id.bt_back3)
        bt_back3.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
        val bt_inscrire_patient = findViewById<TextView>(R.id.bt_inscrire_patient)
        bt_inscrire_patient.setOnClickListener {
            val intent = Intent(this, Inscrire_patient::class.java)
            startActivity(intent)
        }
        val bt_inscrire_doctor = findViewById<TextView>(R.id.bt_inscrire_doctor)
        bt_inscrire_doctor.setOnClickListener {
            val intent = Intent(this, Inscrire_doctor::class.java)
            startActivity(intent)
        }
    }
}