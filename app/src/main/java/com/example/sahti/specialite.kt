package com.example.sahti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.GridLayout
import androidx.cardview.widget.CardView
import android.widget.LinearLayout
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.sahti.DoctorModel
import com.example.sahti.adapters.DoctorsAdapter
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun View.getChildAt(index: Int): View? {
    return if (this is ViewGroup) {
        this.getChildAt(index)
    } else {
        null
    }
}

class specialite : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private lateinit var searchEditText: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_specialite)
        
        auth = FirebaseAuth.getInstance()
        welcomeText = findViewById(R.id.welcomeText)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupBottomNavigation()
        setupSpecialityCards()

        // Configurer la barre de recherche
        searchEditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 3) { // Commencer la recherche après 3 caractères
                    searchDoctors(query)
                }
            }
        })

        // Charger le nom d'utilisateur immédiatement après la création
        loadUserName()
    }

    override fun onResume() {
        super.onResume()
        // Recharger le nom d'utilisateur à chaque fois que l'activité devient visible
        loadUserName()
    }

    private fun loadUserName() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.reference.child("patient")
                .child(currentUser.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val nomPrenom = snapshot.child("nomPrenom").value?.toString() ?: "Utilisateur"
                    val welcomeMessage = "<font face='sans-serif-medium' color='#673AB7'><b>${android.text.Html.escapeHtml(nomPrenom)}</b></font>"
                    welcomeText.text = android.text.Html.fromHtml(welcomeMessage, android.text.Html.FROM_HTML_MODE_COMPACT)
                }
                .addOnFailureListener { e ->
                    Log.e("Specialite", "Erreur lors du chargement du nom d'utilisateur", e)
                    welcomeText.text = "<b>Utilisateur inconnu</b>"
                }
        } else {
            welcomeText.text = "<b>Utilisateur non connecté</b>"
        }
    }



    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_appointments -> {
                    // Naviguer vers l'activité des rendez-vous
                    startActivity(Intent(this, PatientAppointmentsActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profil::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupSpecialityCards() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout2)
        // Récupérer le nom d'utilisateur stocké dans l'activité
        val userName = findViewById<TextView>(R.id.welcomeText).text.toString().substringAfter("\n")
        
        for (i in 0 until gridLayout.childCount) {
            val cardView = gridLayout.getChildAt(i) as? CardView
            cardView?.setOnClickListener { view ->
                // Chercher n'importe quel TextView dans le LinearLayout
                val linearLayout = view.getChildAt(0) as? LinearLayout
                val textView = linearLayout?.findViewWithTag<TextView>("speciality")
                val specialityText = textView?.text?.toString() ?: ""
                
                if (specialityText.isNotEmpty()) {
                    val intent = Intent(this, DoctorsActivity::class.java)
                    intent.putExtra("SPECIALITY", specialityText)
                    // Passer le nom d'utilisateur directement
                    intent.putExtra("USER_NAME", userName)
                    startActivity(intent)
                }
            }
        }
    }

    private fun searchDoctors(query: String) {
        database.reference.child("doctors")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val matchingDoctors = mutableListOf<DoctorModel>()
                    
                    for (doctorSnapshot in snapshot.children) {
                        try {
                            val nom = doctorSnapshot.child("nom").value?.toString() ?: ""
                            val adresse = doctorSnapshot.child("adresse").value?.toString() ?: ""
                            val specialites = doctorSnapshot.child("specialites").value?.toString() ?: ""

                            if (nom.contains(query, ignoreCase = true) || 
                                adresse.contains(query, ignoreCase = true)) {
                                
                                val doctor = DoctorModel(
                                    id = doctorSnapshot.key ?: "",
                                    name = nom,
                                    speciality = specialites,
                                    address = adresse,
                                    photoUrl = doctorSnapshot.child("photo_profil").value?.toString()
                                )
                                matchingDoctors.add(doctor)
                            }
                        } catch (e: Exception) {
                            Log.e("Specialite", "Erreur lors du traitement d'un docteur: ${e.message}")
                        }
                    }

                    if (matchingDoctors.isNotEmpty()) {
                        // Créer et afficher un dialogue avec les résultats
                        showSearchResults(matchingDoctors)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@specialite, 
                        "Erreur lors de la recherche: ${error.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showSearchResults(doctors: List<DoctorModel>) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Résultats de la recherche")
            .create()

        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@specialite)
            adapter = DoctorsAdapter().apply {
                updateDoctors(doctors)
            }
            setPadding(16, 16, 16, 16)
        }

        dialog.setView(recyclerView)
        dialog.show()
    }
}

