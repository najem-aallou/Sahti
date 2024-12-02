package com.example.sahti

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {

    // Instance de FirebaseAuth
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialisation de FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Références aux champs et boutons
        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<TextView>(R.id.btn_log)

        val bt_back2 = findViewById<TextView>(R.id.bt_back2)
        bt_back2.setOnClickListener {
            val intent = Intent(this, Page4::class.java)
            startActivity(intent)
        }

        val bt_inscrire1 = findViewById<TextView>(R.id.bt_inscrire1)
        bt_inscrire1.setOnClickListener {
            val intent = Intent(this, Inscrire_type::class.java)
            startActivity(intent)
        }

        // Action du bouton Login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validation des champs
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Connexion avec Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Connexion réussie
                        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                        // Rediriger vers une autre activité (exemple : HomeActivity)
                        val intent = Intent(this@login, Home::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Échec de connexion
                        Toast.makeText(
                            this,
                            "Erreur : ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
