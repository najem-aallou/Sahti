package com.example.sahti

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.material.button.MaterialButton
import android.os.Handler
import android.os.Looper
import com.google.android.material.bottomnavigation.BottomNavigationView

class DoctorProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var profileImageView: ImageView
    private lateinit var nomText: TextView
    private lateinit var inptText: TextView
    private lateinit var specialitesText: TextView
    private lateinit var adresseText: TextView
    private lateinit var emailText: TextView
    private lateinit var telephoneText: TextView
    private lateinit var aboutText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        try {
            // Initialiser Firebase
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()

            // Vérifier si l'utilisateur est connecté
            if (auth.currentUser == null) {
                startActivity(Intent(this, login::class.java))
                finish()
                return
            }

            // Initialiser les vues
            initializeViews()
            
            // Charger les données
            loadDoctorData()

            // Configurer les boutons
            setupButtons()

            // Configurer la navigation
            setupBottomNavigation()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de l'initialisation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        try {
            profileImageView = findViewById(R.id.profileImageView) ?: throw Exception("profileImageView non trouvé")
            nomText = findViewById(R.id.nomPrenomText) ?: throw Exception("nomPrenomText non trouvé")
            inptText = findViewById(R.id.inptText) ?: throw Exception("inptText non trouvé")
            specialitesText = findViewById(R.id.specialitesText) ?: throw Exception("specialitesText non trouvé")
            adresseText = findViewById(R.id.adresseText) ?: throw Exception("adresseText non trouvé")
            emailText = findViewById(R.id.emailText) ?: throw Exception("emailText non trouvé")
            telephoneText = findViewById(R.id.telephoneText) ?: throw Exception("telephoneText non trouvé")
            aboutText = findViewById(R.id.aboutText) ?: throw Exception("aboutText non trouvé")
        } catch (e: Exception) {
            val message = "Erreur d'initialisation des vues: ${e.message}"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
            // Ne pas terminer l'activité immédiatement pour voir le message d'erreur
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 2000)
        }
    }

    private fun setupButtons() {
        val modifierButton = findViewById<MaterialButton>(R.id.modifierButton)
        modifierButton.setOnClickListener {
            val intent = Intent(this, ModifierDoctorProfile::class.java)
            startActivity(intent)
        }
        val supprimerButton = findViewById<MaterialButton>(R.id.supprimerButton)
        val logoutButton = findViewById<MaterialButton>(R.id.logoutButton)


        supprimerButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
        finish() // Simplement terminer l'activité pour revenir à DoctorHome
    }

    private fun loadDoctorData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.reference.child("doctors").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    try {
                        // Mettre à jour tous les champs avec les nouvelles données
                        nomText.text = snapshot.child("nom").value?.toString() ?: ""
                        inptText.text = snapshot.child("inpt").value?.toString() ?: ""
                        specialitesText.text = snapshot.child("specialites").value?.toString() ?: ""
                        adresseText.text = snapshot.child("adresse").value?.toString() ?: ""
                        emailText.text = snapshot.child("email").value?.toString() ?: ""
                        telephoneText.text = snapshot.child("telephone").value?.toString() ?: ""
                        aboutText.text = snapshot.child("aPropos").value?.toString() ?: ""

                        // Charger la photo de profil
                        val photoUrl = snapshot.child("photo_profil").value?.toString()
                        if (!photoUrl.isNullOrEmpty()) {
                            loadProfileImage(photoUrl)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Erreur lors du chargement des données: ${e.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur lors du chargement des données: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadProfileImage(photoUrl: String) {
        try {
            if (photoUrl.startsWith("data:image")) {
                // Si l'image est en base64
                val base64Image = photoUrl.substringAfter("base64,")
                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImageView.setImageBitmap(bitmap)
            } else {
                // Si c'est une URL normale
                // Vous pouvez utiliser Glide ou Picasso ici si vous préférez
                Thread {
                    try {
                        val url = java.net.URL(photoUrl)
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        Handler(Looper.getMainLooper()).post {
                            profileImageView.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@DoctorProfile, 
                                "Erreur de chargement de l'image: ${e.message}", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors du chargement de l'image: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
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
            database.reference.child("doctors").child(userId).removeValue()
                .addOnSuccessListener {
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

    override fun onResume() {
        super.onResume()
        // Recharger les données à chaque fois que l'activité reprend le focus
        loadDoctorData()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DoctorHome::class.java))
                    false
                }
                R.id.nav_appointments -> {
                    startActivity(Intent(this, DoctorAppointmentsActivity::class.java))
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