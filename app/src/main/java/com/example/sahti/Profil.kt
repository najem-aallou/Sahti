package com.example.sahti

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Profil : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    
    // Ajouter ces variables au niveau de la classe pour y accéder depuis différentes méthodes
    private lateinit var nomPrenomText: TextView
    private lateinit var cinText: TextView
    private lateinit var emailText: TextView
    private lateinit var telephoneText: TextView
    private lateinit var naissanceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        // Initialiser Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialiser les vues
        nomPrenomText = findViewById(R.id.nomPrenomText)
        cinText = findViewById(R.id.cinText)
        emailText = findViewById(R.id.emailText)
        telephoneText = findViewById(R.id.telephoneText)
        naissanceText = findViewById(R.id.naissanceText)
        
        val modifierButton = findViewById<Button>(R.id.modifierButton)
        val supprimerButton = findViewById<Button>(R.id.supprimerButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Charger les données
        loadUserData()

        // Gérer le clic sur le bouton Modifier
        modifierButton.setOnClickListener {
            val intent = Intent(this, ModifierProfil::class.java)
            startActivityForResult(intent, MODIFY_PROFILE_REQUEST)
        }

        // Gérer le clic sur le bouton Supprimer
        supprimerButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Gérer le clic sur le bouton Déconnexion
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child("patient").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    nomPrenomText.text = snapshot.child("nomPrenom").value?.toString() ?: ""
                    cinText.text = snapshot.child("cin").value?.toString() ?: ""
                    emailText.text = snapshot.child("email").value?.toString() ?: ""
                    telephoneText.text = snapshot.child("telephone").value?.toString() ?: ""
                    naissanceText.text = snapshot.child("naissance").value?.toString() ?: ""
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MODIFY_PROFILE_REQUEST && resultCode == RESULT_OK) {
            loadUserData()  // Recharger les données après modification
        }
    }

    companion object {
        private const val MODIFY_PROFILE_REQUEST = 100
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer le compte")
            .setMessage("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteAccount() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Supprimer les données de la base de données
            database.child("patient").child(userId).removeValue()
                .addOnSuccessListener {
                    // Supprimer le compte Firebase Auth
                    auth.currentUser?.delete()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Compte supprimé avec succès", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, login::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this, "Erreur lors de la suppression du compte", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erreur lors de la suppression des données", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, specialite::class.java))
                    false
                }
                R.id.nav_appointments -> {
                    startActivity(Intent(this, PatientAppointmentsActivity::class.java))
                    false
                }
                R.id.nav_profile -> true // Déjà sur la page de profil
                else -> false
            }
        }
        // Sélectionner l'item actif
        bottomNav.selectedItemId = R.id.nav_profile
    }

} 