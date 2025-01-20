package com.example.sahti

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.adapters.DoctorAppointmentsAdapter
import com.example.sahti.models.Appointment
import com.example.sahti.models.AppointmentWithPatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.widget.ImageView

class DoctorAppointmentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: DoctorAppointmentsAdapter
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_appointments)

        setupRecyclerView()
        loadValidatedAppointments()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.validatedAppointmentsRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        
        adapter = DoctorAppointmentsAdapter(
            onValidateClick = { }, // Pas besoin de ces actions pour les rendez-vous validés
            onRejectClick = { }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DoctorAppointmentsActivity)
            adapter = this@DoctorAppointmentsActivity.adapter
        }
    }

    private fun loadValidatedAppointments() {
        val doctorId = auth.currentUser?.uid ?: return
        
        database.reference.child("appointments")
            .orderByChild("doctorId")
            .equalTo(doctorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val appointmentsList = mutableListOf<AppointmentWithPatient>()
                    var validatedRequests = 0
                    var completedRequests = 0
                    
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        if (appointment != null && appointment.status == "validated") {
                            validatedRequests++
                            database.reference.child("patient")
                                .child(appointment.patientId)
                                .get()
                                .addOnSuccessListener { patientSnapshot ->
                                    val patientName = patientSnapshot.child("nomPrenom").value?.toString() ?: ""
                                    
                                    if (patientName.isNotEmpty()) {
                                        appointmentsList.add(
                                            AppointmentWithPatient(
                                                id = appointmentSnapshot.key ?: "",
                                                doctorId = appointment.doctorId,
                                                patientId = appointment.patientId,
                                                patientName = patientName,
                                                date = appointment.date,
                                                timeSlot = appointment.timeSlot,
                                                status = appointment.status
                                            )
                                        )
                                    }
                                    
                                    completedRequests++
                                    if (completedRequests == validatedRequests) {
                                        updateUI(appointmentsList.sortedBy { it.date })
                                    }
                                }
                        }
                    }
                    
                    if (validatedRequests == 0) {
                        updateUI(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DoctorAppointmentsActivity,
                        "Erreur lors du chargement des rendez-vous",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateUI(appointments: List<AppointmentWithPatient>) {
        val emptyStateImage = findViewById<ImageView>(R.id.emptyStateImage)
        
        if (appointments.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyStateImage.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            emptyStateImage.visibility = View.GONE
            adapter.updateAppointments(appointments)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DoctorHome::class.java))
                    false
                }
                R.id.nav_appointments -> true // Déjà sur la page des rendez-vous
                R.id.nav_profile -> {
                    startActivity(Intent(this, DoctorProfile::class.java))
                    false
                }
                else -> false
            }
        }
        // Sélectionner l'item actif
        bottomNav.selectedItemId = R.id.nav_appointments
    }
} 