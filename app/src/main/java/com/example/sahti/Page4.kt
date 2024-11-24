package com.example.sahti

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Page4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bt_back1 = findViewById<TextView>(R.id.bt_back1)
        bt_back1.setOnClickListener {
            val intent = Intent(this, Page3::class.java)
            startActivity(intent)
        }
        val bt3 = findViewById<TextView>(R.id.bt3)
      /*  bt3.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }*/
    }
}