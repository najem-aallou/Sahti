package com.example.sahti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.adapters.PatientAppointmentsAdapter
import com.example.sahti.models.Appointment
import com.example.sahti.models.AppointmentWithDoctor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class PatientAppointmentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: PatientAppointmentsAdapter
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_appointments)

        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        emptyView = findViewById(R.id.emptyView)

        setupRecyclerView()
        loadAppointments()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        adapter = PatientAppointmentsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadAppointments() {
        val userId = auth.currentUser?.uid ?: return
        
        database.reference.child("appointments")
            .orderByChild("patientId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val appointmentsList = mutableListOf<AppointmentWithDoctor>()
                    
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        if (appointment != null) {
                            // Charger les informations du médecin
                            database.reference.child("doctors")
                                .child(appointment.doctorId)
                                .get()
                                .addOnSuccessListener { doctorSnapshot ->
                                    val doctorName = doctorSnapshot.child("nom").value?.toString() ?: ""
                                    val specialites = doctorSnapshot.child("specialites")
                                        .children.mapNotNull { it.value?.toString() }
                                        .joinToString(", ")

                                    appointmentsList.add(
                                        AppointmentWithDoctor(
                                            id = appointment.id,
                                            doctorId = appointment.doctorId,
                                            doctorName = doctorName,
                                            doctorSpeciality = specialites,
                                            patientId = appointment.patientId,
                                            date = appointment.date,
                                            timeSlot = appointment.timeSlot,
                                            status = appointment.status,
                                            createdAt = appointment.createdAt
                                        )
                                    )

                                    // Mettre à jour l'interface utilisateur
                                    updateUI(appointmentsList)
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gérer l'erreur
                }
            })
    }

    private fun updateUI(appointments: List<AppointmentWithDoctor>) {
        if (appointments.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            adapter.updateAppointments(appointments.sortedByDescending { it.date })
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
                R.id.nav_appointments -> true // Déjà sur la page des rendez-vous
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profil::class.java))
                    false
                }
                else -> false
            }
        }
        // Sélectionner l'item actif
        bottomNav.selectedItemId = R.id.nav_appointments
    }
} 