package com.example.sahti

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import com.example.sahti.models.Appointment
import android.util.Log

class AppointmentConfirmationActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_confirmation)

        val doctorId = intent.getStringExtra("DOCTOR_ID")
        val dateMillis = intent.getLongExtra("DATE", 0)
        val timeSlot = intent.getStringExtra("TIME_SLOT")

        if (doctorId == null || dateMillis == 0L || timeSlot == null) {
            Toast.makeText(this, "Informations manquantes", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDoctorInfo(doctorId)
        displayAppointmentInfo(dateMillis, timeSlot)
        setupButtons(doctorId, dateMillis, timeSlot)
    }

    private fun loadDoctorInfo(doctorId: String) {
        database.reference.child("doctors").child(doctorId).get()
            .addOnSuccessListener { snapshot ->
                val doctorName = snapshot.child("nom").value?.toString() ?: ""
                val speciality = snapshot.child("specialites").value?.toString() ?: ""

                findViewById<TextView>(R.id.doctorNameText).text = "Dr. $doctorName"
                findViewById<TextView>(R.id.specialityText).text = speciality
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors du chargement des informations", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayAppointmentInfo(dateMillis: Long, timeSlot: String) {
        val date = Date(dateMillis)
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        
        findViewById<TextView>(R.id.dateText).text = "Date : ${dateFormat.format(date)}"
        findViewById<TextView>(R.id.timeText).text = "Heure : $timeSlot"
    }

    private fun setupButtons(doctorId: String, dateMillis: Long, timeSlot: String) {
        findViewById<MaterialButton>(R.id.confirmButton).setOnClickListener {
            saveAppointment(doctorId, dateMillis, timeSlot)
        }

        findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            finish()
        }
    }

    private fun saveAppointment(doctorId: String, dateMillis: Long, timeSlot: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show()
            return
        }

        // Créer le nouveau rendez-vous
        val appointment = Appointment(
            doctorId = doctorId,
            patientId = userId,
            date = dateMillis,
            timeSlot = timeSlot,
            status = "pending"
        )

        // Sauvegarder directement dans Firebase
        val newAppointmentRef = database.reference.child("appointments").push()
        appointment.copy(id = newAppointmentRef.key ?: "").let {
            newAppointmentRef.setValue(it)
                .addOnSuccessListener {
                    Toast.makeText(this, "Rendez-vous enregistré, en attente de validation", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("Appointment", "Erreur d'enregistrement: ${e.message}")
                    Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 