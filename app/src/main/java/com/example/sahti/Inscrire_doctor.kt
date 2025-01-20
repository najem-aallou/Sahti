package com.example.sahti

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.ImageView
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import android.widget.MultiAutoCompleteTextView
import android.widget.ArrayAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.google.android.material.checkbox.MaterialCheckBox

class Inscrire_doctor : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private lateinit var imgProfile: ImageView
    private lateinit var telephoneInput: TextInputEditText
    private lateinit var aProposInput: TextInputEditText

    companion object {
        private const val PERMISSION_CODE = 1001
        private const val IMAGE_PICK_CODE = 1000
        private const val IMGUR_CLIENT_ID = "votre_client_id_imgur"  // Inscrivez-vous sur imgur.com/developer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscrire_doctor)

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = Firebase.storage

        // Initialisation des vues
        imgProfile = findViewById(R.id.imgProfile)
        val btnSelectPhoto = findViewById<MaterialButton>(R.id.btnSelectPhoto)
        val editNom = findViewById<TextInputEditText>(R.id.editNom)
        val editInpt = findViewById<TextInputEditText>(R.id.editInpt)
        val editSpecialites = findViewById<MultiAutoCompleteTextView>(R.id.editSpecialites)
        val editAdresse = findViewById<TextInputEditText>(R.id.editAdresse)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnInscrire = findViewById<MaterialButton>(R.id.btnInscrire)
        
        // Correction des IDs pour correspondre au layout
        telephoneInput = findViewById(R.id.telephoneLayout)
        aProposInput = findViewById(R.id.aProposLayout)

        // Liste des spécialités
        val specialites = arrayOf(
            "Généraliste", "Néphrologue", "Nutrisionniste",
            "Gastro-entérologie", "Dentaire", "Neurologie",
            "Ophtalmologie", "Orthopédie", "Pédiatrie", 
            "ORL", "Pneumologie", "Cardiologie"
        )

        // Adapter pour les spécialités
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            specialites
        )

        editSpecialites.setAdapter(adapter)
        editSpecialites.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        btnSelectPhoto.setOnClickListener {
            if (checkPermission()) {
                pickImageFromGallery()
            } else {
                requestPermission()
            }
        }

        btnInscrire.setOnClickListener {
            val nom = editNom.text.toString()
            val inpt = editInpt.text.toString()
            val specialites = editSpecialites.text.toString()
            val adresse = editAdresse.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val conditionsAccepted = findViewById<MaterialCheckBox>(R.id.cbConditions).isChecked

            // Vérifier si tous les champs sont remplis et les conditions acceptées
            if (nom.isEmpty() || inpt.isEmpty() || specialites.isEmpty() || 
                adresse.isEmpty() || email.isEmpty() || password.isEmpty() || !conditionsAccepted) {
                Toast.makeText(this, "Veuillez remplir tous les champs et accepter les conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerDoctor(nom, inpt, specialites, adresse, email, password)
        }
    }

    private fun validateInputs(
        nom: String, inpt: String, specialites: String,
        adresse: String, email: String, password: String
    ): Boolean {
        val telephone = telephoneInput.text.toString().trim()

        if (nom.isEmpty() || inpt.isEmpty() || specialites.isEmpty() ||
            adresse.isEmpty() || email.isEmpty() || password.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!telephone.matches(Regex("^[0-9]{10}$"))) {
            Toast.makeText(this, "Veuillez entrer un numéro de téléphone valide", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerDoctor(nom: String, inpt: String, specialites: String, adresse: String, email: String, password: String) {
        // Récupérer les valeurs de téléphone et à propos
        val telephone = telephoneInput.text.toString().trim()
        val aPropos = aProposInput.text.toString().trim()

        // Vérifier si tous les champs sont valides
        if (!validateInputs(nom, inpt, specialites, adresse, email, password)) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                
                // Uploader l'image de profil
                uploadProfileImage(userId) { imageUrl ->
                    // Créer un Map avec toutes les données du docteur, incluant téléphone et à propos
                    val doctorData = hashMapOf(
                        "nom" to nom,
                        "inpt" to inpt,
                        "specialites" to specialites,
                        "adresse" to adresse,
                        "email" to email,
                        "telephone" to telephone,  // Ajout du téléphone
                        "aPropos" to aPropos,     // Ajout de l'à propos
                        "photo_profil" to imageUrl,
                        "role" to "doctor"
                    )

                    // Sauvegarder dans Firebase
                    database.reference.child("doctors")
                        .child(userId)
                        .setValue(doctorData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, login::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur d'authentification : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImage(userId: String, onComplete: (String) -> Unit) {
        if (selectedImageUri == null) {
            onComplete("")
            return
        }

        try {
            // Convertir l'image en Base64
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val bytes = inputStream?.readBytes()
            val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            
            // Stocker directement le Base64 dans la base de données
            onComplete("data:image/jpeg;base64,$base64Image")
            
        } catch (e: Exception) {
            onComplete("")
            Toast.makeText(this, "Erreur lors du traitement de l'image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedImageUri = data?.data
            imgProfile.setImageURI(selectedImageUri)
        }
    }
}
