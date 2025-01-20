package com.example.sahti

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.adapters.DoctorsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class DoctorsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DoctorsAdapter
    private val database = FirebaseDatabase.getInstance()
    private lateinit var searchEditText: EditText
    private var allDoctors = listOf<DoctorModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docteurs)

        // Récupérer la spécialité sélectionnée
        val speciality = intent.getStringExtra("SPECIALITY")
        if (speciality == null) {
            Toast.makeText(this, "Erreur: spécialité non spécifiée", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        // Configurer le titre
        findViewById<TextView>(R.id.titleText).text = speciality

        // Configurer la barre de recherche
        searchEditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterDoctors(s.toString())
            }
        })

        // Configurer le RecyclerView
        recyclerView = findViewById(R.id.doctorsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DoctorsAdapter()
        recyclerView.adapter = adapter

        // Charger les docteurs
        loadDoctors(speciality)

        setupBottomNavigation()
    }

    private fun filterDoctors(query: String) {
        if (query.isEmpty()) {
            adapter.updateDoctors(allDoctors)
            return
        }

        val filteredDoctors = allDoctors.filter { doctor ->
            doctor.name.contains(query, ignoreCase = true) ||
            doctor.address.contains(query, ignoreCase = true)
        }
        adapter.updateDoctors(filteredDoctors)
    }

    private fun loadDoctors(speciality: String) {
        try {
            database.reference.child("doctors")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val doctors = mutableListOf<DoctorModel>()
                        for (doctorSnapshot in snapshot.children) {
                            try {
                                val specialitesString = doctorSnapshot.child("specialites").value?.toString() ?: ""
                                Log.d("DoctorsActivity", "Spécialités trouvées: $specialitesString pour docteur ${doctorSnapshot.key}")
                                
                                if (specialitesString.contains(speciality, ignoreCase = true)) {
                                    val doctor = DoctorModel(
                                        id = doctorSnapshot.key ?: "",
                                        name = doctorSnapshot.child("nom").value?.toString() ?: "",
                                        speciality = speciality,
                                        address = doctorSnapshot.child("adresse").value?.toString() ?: "",
                                        photoUrl = doctorSnapshot.child("photo_profil").value?.toString()
                                    )
                                    doctors.add(doctor)
                                    Log.d("DoctorsActivity", "Docteur ajouté: ${doctor.name}")
                                }
                            } catch (e: Exception) {
                                Log.e("DoctorsActivity", "Erreur lors du traitement d'un docteur: ${e.message}")
                            }
                        }
                        
                        if (doctors.isEmpty()) {
                            Toast.makeText(this@DoctorsActivity, 
                                "Aucun médecin trouvé pour cette spécialité", 
                                Toast.LENGTH_SHORT).show()
                        }
                        
                        // Mettre à jour allDoctors avec la nouvelle liste
                        allDoctors = doctors
                        // Mettre à jour l'adapter avec la nouvelle liste
                        adapter.updateDoctors(doctors)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DoctorsActivity, 
                            "Erreur lors du chargement des données: ${error.message}", 
                            Toast.LENGTH_SHORT).show()
                        Log.e("DoctorsActivity", "Erreur Firebase: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e("DoctorsActivity", "Erreur générale: ${e.message}")
            Toast.makeText(this, 
                "Une erreur s'est produite: ${e.message}", 
                Toast.LENGTH_SHORT).show()
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profil::class.java))
                    false
                }
                else -> false
            }
        }
    }


} 