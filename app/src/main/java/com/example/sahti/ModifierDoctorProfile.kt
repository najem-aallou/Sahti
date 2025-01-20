package com.example.sahti

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.net.Uri
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.EmailAuthProvider

class ModifierDoctorProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modifier_doctor_profile)

        // Initialiser Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialiser profileImageView
        profileImageView = findViewById(R.id.editProfileImageView)

        // Initialisation des vues
        val editAdresse = findViewById<TextInputEditText>(R.id.editAdresse)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editTelephone = findViewById<TextInputEditText>(R.id.editTelephone)
        val editAbout = findViewById<TextInputEditText>(R.id.editAbout)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val editCurrentPassword = findViewById<TextInputEditText>(R.id.editCurrentPassword)
        val saveButton = findViewById<MaterialButton>(R.id.saveButton)

        // Charger les données actuelles
        loadCurrentData()

        // Gérer la sauvegarde
        saveButton.setOnClickListener {
            val currentPassword = editCurrentPassword.text?.toString()
            if (currentPassword.isNullOrEmpty()) {
                Toast.makeText(this, "Veuillez entrer votre mot de passe actuel", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vérifier le mot de passe actuel
            val user = auth.currentUser
            val email = user?.email
            if (email != null) {
                // Réauthentifier l'utilisateur
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Le mot de passe est correct, procéder aux modifications
                        updateDoctorProfile(
                            editAdresse.text.toString(),
                            editEmail.text.toString(),
                            editTelephone.text.toString(),
                            editAbout.text.toString(),
                            editPassword.text.toString()
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadCurrentData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.reference.child("doctors").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    // Charger les données existantes
                    findViewById<TextInputEditText>(R.id.editAdresse).setText(snapshot.child("adresse").value?.toString())
                    findViewById<TextInputEditText>(R.id.editEmail).setText(snapshot.child("email").value?.toString())
                    findViewById<TextInputEditText>(R.id.editTelephone).setText(snapshot.child("telephone").value?.toString())
                    findViewById<TextInputEditText>(R.id.editAbout).setText(snapshot.child("aPropos").value?.toString())

                    // Charger l'image de profil
                    val photoUrl = snapshot.child("photo_profil").value?.toString()
                    if (!photoUrl.isNullOrEmpty()) {
                        loadProfileImage(photoUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show()
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
                Thread {
                    try {
                        val url = java.net.URL(photoUrl)
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        Handler(Looper.getMainLooper()).post {
                            profileImageView.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@ModifierDoctorProfile,
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

    private fun updateDoctorProfile(
        adresse: String,
        email: String,
        telephone: String,
        about: String,
        newPassword: String
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val updates = hashMapOf<String, Any>(
                "adresse" to adresse,
                "email" to email,
                "telephone" to telephone,
                "aPropos" to about
            )

            // Mettre à jour les données dans Firebase
            database.reference.child("doctors").child(userId)
                .updateChildren(updates)
                .addOnSuccessListener {
                    // Si un nouveau mot de passe est fourni, le mettre à jour
                    if (newPassword.isNotEmpty()) {
                        auth.currentUser?.updatePassword(newPassword)
                            ?.addOnSuccessListener {
                                Toast.makeText(this, "Profil et mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(this, "Erreur lors de la mise à jour du mot de passe : ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur lors de la mise à jour : ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 