package com.example.sahti

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class login : AppCompatActivity() {

    // Instance de FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialisation de FirebaseAuth et Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Références aux champs et boutons
        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<TextView>(R.id.btn_log)



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
            signIn(email, password)
        }

        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email de réinitialisation envoyé", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Vérifier d'abord dans la collection doctors
                        database.child("doctors").child(userId).get()
                            .addOnSuccessListener { doctorSnapshot ->
                                if (doctorSnapshot.exists()) {
                                    // C'est un docteur
                                    val intent = Intent(this@login, DoctorHome::class.java)
                                    intent.putExtra("DOCTOR_ID", userId)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Vérifier dans la collection patients
                                    database.child("patient").child(userId).get()
                                        .addOnSuccessListener { patientSnapshot ->
                                            if (patientSnapshot.exists()) {
                                                // C'est un patient
                                                val nomPrenom = patientSnapshot.child("nomPrenom").value?.toString() ?: "Utilisateur"
                                                val intent = Intent(this@login, specialite::class.java)
                                                intent.putExtra("USER_NAME", nomPrenom)
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Échec de la connexion", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
