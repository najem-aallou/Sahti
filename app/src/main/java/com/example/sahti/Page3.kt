package com.example.sahti

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Page3 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bt2 = findViewById<TextView>(R.id.bt2)
        bt2.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }

        val bt_back = findViewById<TextView>(R.id.bt_back)
        bt_back.setOnClickListener {
            val intent = Intent(this, Page2::class.java)
            startActivity(intent)
        }
        val bt_sauter = findViewById<TextView>(R.id.bt_sauter)
        bt_sauter.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
}