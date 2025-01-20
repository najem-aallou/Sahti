package com.example.sahti

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialite)

        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_appointments -> {
                    startActivity(Intent(this, PatientAppointmentsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profil::class.java))
                    true
                }
                else -> false
            }
        }
    }
}