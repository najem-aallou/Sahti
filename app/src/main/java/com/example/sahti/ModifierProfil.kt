package com.example.sahti

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser

class ModifierProfil : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modifier_profil)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Récupérer les champs
        val editNomPrenom = findViewById<TextInputEditText>(R.id.editNomPrenom)
        val editCin = findViewById<TextInputEditText>(R.id.editCin)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editTelephone = findViewById<TextInputEditText>(R.id.editTelephone)
        val editNaissance = findViewById<TextInputEditText>(R.id.editNaissance)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val editCurrentPassword = findViewById<TextInputEditText>(R.id.editCurrentPassword)
        val saveButton = findViewById<MaterialButton>(R.id.saveButton)

        // Charger les données actuelles
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.reference.child("patient").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    editNomPrenom.setText(snapshot.child("nomPrenom").value?.toString())
                    editCin.setText(snapshot.child("cin").value?.toString())
                    editEmail.setText(snapshot.child("email").value?.toString())
                    editTelephone.setText(snapshot.child("telephone").value?.toString())
                    editNaissance.setText(snapshot.child("naissance").value?.toString())
                }
        }

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
                        processUpdates(user, editEmail, editPassword, userId, editNomPrenom, editCin, editTelephone, editNaissance)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun processUpdates(
        currentUser: FirebaseUser,
        editEmail: TextInputEditText,
        editPassword: TextInputEditText,
        userId: String?,
        editNomPrenom: TextInputEditText,
        editCin: TextInputEditText,
        editTelephone: TextInputEditText,
        editNaissance: TextInputEditText
    ) {
        val newEmail = editEmail.text?.toString() ?: ""
        val newPassword = editPassword.text?.toString()

        val emailChanged = newEmail != currentUser.email
        val passwordChanged = !newPassword.isNullOrEmpty()

        when {
            emailChanged && passwordChanged -> {
                updateEmailAndPassword(currentUser, newEmail, newPassword!!, userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
            }
            emailChanged -> {
                updateEmail(currentUser, newEmail, userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
            }
            passwordChanged -> {
                updatePassword(currentUser, newPassword!!, userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
            }
            else -> {
                updateUserData(userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
            }
        }
    }

    private fun updateUserData(
        userId: String?,
        editNomPrenom: TextInputEditText,
        editCin: TextInputEditText,
        editEmail: TextInputEditText,
        editTelephone: TextInputEditText,
        editNaissance: TextInputEditText
    ) {
        val updates = hashMapOf<String, Any>(
            "nomPrenom" to (editNomPrenom.text?.toString() ?: ""),
            "cin" to (editCin.text?.toString() ?: ""),
            "email" to (editEmail.text?.toString() ?: ""),
            "telephone" to (editTelephone.text?.toString() ?: ""),
            "naissance" to (editNaissance.text?.toString() ?: "")
        )

        userId?.let { uid ->
            database.reference.child("patient").child(uid)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateEmailAndPassword(
        currentUser: FirebaseUser?,
        newEmail: String,
        newPassword: String,
        userId: String?,
        editNomPrenom: TextInputEditText,
        editCin: TextInputEditText,
        editEmail: TextInputEditText,
        editTelephone: TextInputEditText,
        editNaissance: TextInputEditText
    ) {
        currentUser?.updateEmail(newEmail)
            ?.addOnSuccessListener {
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        updateUserData(userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
                        Toast.makeText(this, "Email et mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erreur lors de la mise à jour du mot de passe : ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors de la mise à jour de l'email : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmail(
        currentUser: FirebaseUser?,
        newEmail: String,
        userId: String?,
        editNomPrenom: TextInputEditText,
        editCin: TextInputEditText,
        editEmail: TextInputEditText,
        editTelephone: TextInputEditText,
        editNaissance: TextInputEditText
    ) {
        currentUser?.updateEmail(newEmail)
            ?.addOnSuccessListener {
                updateUserData(userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
                Toast.makeText(this, "Email mis à jour avec succès", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors de la mise à jour de l'email : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePassword(
        currentUser: FirebaseUser?,
        newPassword: String,
        userId: String?,
        editNomPrenom: TextInputEditText,
        editCin: TextInputEditText,
        editEmail: TextInputEditText,
        editTelephone: TextInputEditText,
        editNaissance: TextInputEditText
    ) {
        currentUser?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                updateUserData(userId, editNomPrenom, editCin, editEmail, editTelephone, editNaissance)
                Toast.makeText(this, "Mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors de la mise à jour du mot de passe : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 