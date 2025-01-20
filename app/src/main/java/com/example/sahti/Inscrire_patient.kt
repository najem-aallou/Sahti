package com.example.sahti

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sahti.databinding.ActivityInscrirePatientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class Inscrire_patient : AppCompatActivity() {

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // View Binding
    private lateinit var binding: ActivityInscrirePatientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityInscrirePatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set click listener for the "Inscrire" button
        binding.btInscrire.setOnClickListener {
            val nomPrenom = binding.etNomPrenom.text.toString()
            val cin = binding.etCin.text.toString()
            val naissance = binding.etNaissance.text.toString()
            val telephone = binding.etTelephone.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val conditionsAccepted = binding.cbConditions.isChecked

            // Validate fields
            if (nomPrenom.isEmpty() || cin.isEmpty() || naissance.isEmpty() || telephone.isEmpty() ||
                email.isEmpty() || password.isEmpty() || !conditionsAccepted
            ) {
                Toast.makeText(
                    this,
                    "Veuillez remplir tous les champs et accepter les conditions",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Create user in Firebase Authentication
                createUserInFirebaseAuth(nomPrenom, cin, naissance, telephone, email, password)
            }
        }

        // Set DatePickerDialog for Date of Birth (naissance)
        binding.etNaissance.setOnClickListener {
            showDatePickerDialog()
        }
    }

    /**
     * Fonction pour afficher un sélecteur de date.
     */

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Créer un DatePickerDialog sans style personnalisé (pour conserver les boutons par défaut)
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format de la date sélectionnée
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.etNaissance.setText(selectedDate) // Afficher la date dans le champ EditText
            },
            year,
            month,
            day
        )

        // Afficher le DatePickerDialog
        datePickerDialog.show()
    }




    /**
     * Fonction pour créer un utilisateur dans Firebase Authentication.
     */
    private fun createUserInFirebaseAuth(
        nomPrenom: String,
        cin: String,
        naissance: String,
        telephone: String,
        email: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Utilisateur ajouté à Firebase Auth, ajoutez les informations dans Realtime Database
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        ajouterPatientDansDatabase(userId, nomPrenom, cin, naissance, telephone, email)
                    }
                } else {
                    // Afficher un message d'erreur en cas d'échec
                    Toast.makeText(
                        this,
                        "Erreur lors de l'inscription : ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Fonction pour ajouter les informations du patient dans Firebase Realtime Database.
     */
    private fun ajouterPatientDansDatabase(
        userId: String,
        nomPrenom: String,
        cin: String,
        naissance: String,
        telephone: String,
        email: String
    ) {
        // Créer un objet pour les données du patient
        val patientData = mapOf(
            "id" to userId,
            "nomPrenom" to nomPrenom,
            "cin" to cin,
            "naissance" to naissance,
            "telephone" to telephone,
            "email" to email
        )

        // Ajouter les données dans Firebase Realtime Database
        database.child("patient").child(userId).setValue(patientData)
            .addOnSuccessListener {
                Toast.makeText(this, "Inscription réussie et patient ajouté avec succès", Toast.LENGTH_SHORT).show()
                // Naviguer vers la page de login
                val intent = Intent(this, login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // Fermer l'activité actuelle
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors de l'ajout dans la base de données : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
