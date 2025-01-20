package com.example.sahti

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.adapters.DoctorAppointmentsAdapter
import com.example.sahti.models.AppointmentWithPatient
import com.example.sahti.models.Appointment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Log

class DoctorHome : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var appointmentsAdapter: DoctorAppointmentsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Charger le nom du docteur
        loadDoctorName()

        // Configuration de la barre de navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_appointments -> {
                    startActivity(Intent(this, DoctorAppointmentsActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, DoctorProfile::class.java))
                    false
                }
                else -> false
            }
        }

        setupAppointmentsRecyclerView()
        loadPendingAppointments()
    }

    override fun onResume() {
        super.onResume()
        loadPendingAppointments() // Recharger les rendez-vous à la reprise
    }

    private fun setupAppointmentsRecyclerView() {
        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        emptyView = findViewById(R.id.emptyAppointmentsView)
        
        appointmentsAdapter = DoctorAppointmentsAdapter(
            onValidateClick = { appointment -> validateAppointment(appointment) },
            onRejectClick = { appointment -> rejectAppointment(appointment) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DoctorHome)
            adapter = appointmentsAdapter
        }
    }

    private fun loadPendingAppointments() {
        val doctorId = auth.currentUser?.uid ?: return
        
        database.reference.child("appointments")
            .orderByChild("doctorId")
            .equalTo(doctorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val appointmentsList = mutableListOf<AppointmentWithPatient>()
                    var pendingRequests = 0
                    var completedRequests = 0
                    
                    if (!snapshot.exists()) {
                        updateUI(appointmentsList)
                        return
                    }
                    
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        if (appointment != null && appointment.status == "pending") {
                            pendingRequests++
                            // Charger les informations du patient
                            database.reference.child("patient")
                                .child(appointment.patientId)
                                .get()
                                .addOnSuccessListener { patientSnapshot ->
                                    // Récupérer directement le nomPrenom
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
                                    if (completedRequests == pendingRequests) {
                                        val sortedAppointments = appointmentsList.sortedBy { it.date }
                                        updateUI(sortedAppointments)
                                    }
                                }
                                .addOnFailureListener {
                                    completedRequests++
                                    if (completedRequests == pendingRequests) {
                                        updateUI(appointmentsList)
                                    }
                                }
                        }
                    }
                    
                    if (pendingRequests == 0) {
                        updateUI(appointmentsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DoctorHome, 
                        "Erreur lors du chargement des rendez-vous", 
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun validateAppointment(appointment: AppointmentWithPatient) {
        database.reference.child("appointments")
            .child(appointment.id)
            .child("status")
            .setValue("validated")
            .addOnSuccessListener {
                Toast.makeText(this, "Rendez-vous validé", Toast.LENGTH_SHORT).show()
                // Le listener ValueEventListener dans loadPendingAppointments() 
                // mettra automatiquement à jour l'interface
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors de la validation", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectAppointment(appointment: AppointmentWithPatient) {
        database.reference.child("appointments")
            .child(appointment.id)
            .child("status")
            .setValue("rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Rendez-vous refusé", Toast.LENGTH_SHORT).show()
                // Le listener ValueEventListener dans loadPendingAppointments() 
                // mettra automatiquement à jour l'interface
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors du refus", Toast.LENGTH_SHORT).show()
            }
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
            appointmentsAdapter.updateAppointments(appointments.sortedByDescending { it.date })
        }
    }

    private fun loadDoctorName() {
        val doctorId = auth.currentUser?.uid ?: return
        val welcomeText = findViewById<TextView>(R.id.welcomeDoctorText)
        
        Log.d("DoctorHome", "DoctorId: $doctorId")
        
        database.reference.child("doctors")
            .child(doctorId)
            .get()
            .addOnSuccessListener { doctorSnapshot ->
                Log.d("DoctorHome", "Doctor snapshot: ${doctorSnapshot.value}")
                
                val doctorName = doctorSnapshot.child("nom").value?.toString() ?: ""
                Log.d("DoctorHome", "Doctor name: $doctorName")
                
                runOnUiThread {
                    if (doctorName.isNotEmpty()) {
                        welcomeText.text = "Welcome Dr. $doctorName"
                        welcomeText.invalidate() // Force le rafraîchissement du TextView
                    } else {
                        welcomeText.text = "Welcome Doctor"
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DoctorHome", "Erreur lors de la récupération du docteur", e)
                runOnUiThread {
                    welcomeText.text = "Welcome Doctor"
                }
            }
    }
} 