package com.example.sahti

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.example.sahti.adapters.DaysAdapter
import com.example.sahti.adapters.TimeSlotsAdapter
import com.example.sahti.models.Appointment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class DoctorDetailActivity : AppCompatActivity() {
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var timeSlotsAdapter: TimeSlotsAdapter
    private var selectedDate: Calendar? = null
    private var selectedTimeSlot: String? = null
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_detail)

        setupViews()
        setupCalendar()
        setupTimeSlots()
        setupBookingButton()

        val doctorId = intent.getStringExtra("DOCTOR_ID")
        if (doctorId != null) {
            loadDoctorData(doctorId)
        }

        setupBottomNavigation()
    }

    private fun setupViews() {
        // Configuration du RecyclerView pour les jours
        daysAdapter = DaysAdapter { date ->
            selectedDate = date
            updateAvailableTimeSlots(date)
        }
        findViewById<RecyclerView>(R.id.daysRecyclerView).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
        }

        // Configuration du RecyclerView pour les créneaux horaires
        timeSlotsAdapter = TimeSlotsAdapter { timeSlot ->
            selectedTimeSlot = timeSlot
        }
        findViewById<RecyclerView>(R.id.timeSlotsRecyclerView).apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = timeSlotsAdapter
        }
    }

    private fun loadDoctorData(doctorId: String) {
        val database = FirebaseDatabase.getInstance()
        database.reference.child("doctors").child(doctorId).get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.child("nom").value?.toString() ?: ""
                val speciality = snapshot.child("specialites").value?.toString() ?: ""
                val photoUrl = snapshot.child("photo_profil").value?.toString()
                val about = snapshot.child("aPropos").value?.toString() ?: "Aucune description disponible"
                val phone = snapshot.child("telephone").value?.toString() ?: ""
                val inpt = snapshot.child("inpt").value?.toString() ?: ""
                val address = snapshot.child("adresse").value?.toString() ?: ""

                findViewById<TextView>(R.id.doctorName).text = name
                findViewById<TextView>(R.id.doctorSpeciality).text = "Spécialité : $speciality"
                findViewById<TextView>(R.id.doctorDescription).text = about
                findViewById<TextView>(R.id.doctorPhone).text = "Téléphone : $phone"
                findViewById<TextView>(R.id.doctorAddress).text = "Adresse : $address"

                loadDoctorPhoto(photoUrl)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDoctorPhoto(photoUrl: String?) {
        val imageView = findViewById<ImageView>(R.id.doctorImage)
        photoUrl?.let { url ->
            try {
                if (url.startsWith("data:image")) {
                    val base64Image = url.substringAfter("base64,")
                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.default_profile)
            }
        } ?: imageView.setImageResource(R.drawable.default_profile)
    }

    private fun setupCalendar() {
        val days = mutableListOf<Calendar>()
        val today = Calendar.getInstance()
        
        // Ajouter les 7 prochains jours (en excluant les dimanches)
        var i = 0
        while (days.size < 7) {
            val day = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, i)
            }
            
            // Vérifier si le jour n'est pas un dimanche (Calendar.SUNDAY = 1)
            if (day.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                days.add(day)
            }
            i++
        }
        
        daysAdapter.submitList(days)
    }

    private fun setupTimeSlots() {
        val timeSlots = listOf(
            "09:00", "10:00", "11:00",
            "14:00", "15:00", "16:00",
            "17:00", "18:00"
        )
        timeSlotsAdapter.submitList(timeSlots)
    }

    private fun updateAvailableTimeSlots(date: Calendar) {
        // Liste de tous les créneaux possibles
        val allTimeSlots = listOf(
            "09:00", "10:00", "11:00",
            "14:00", "15:00", "16:00",
            "17:00", "18:00"
        )
        
        // Afficher directement tous les créneaux disponibles
        timeSlotsAdapter.submitList(allTimeSlots)
    }

    private fun setupBookingButton() {
        findViewById<MaterialButton>(R.id.bookAppointmentButton).setOnClickListener {
            if (selectedDate == null || selectedTimeSlot == null) {
                Toast.makeText(this, "Veuillez sélectionner une date et un horaire", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, AppointmentConfirmationActivity::class.java).apply {
                putExtra("DOCTOR_ID", intent.getStringExtra("DOCTOR_ID"))
                putExtra("DATE", selectedDate?.timeInMillis)
                putExtra("TIME_SLOT", selectedTimeSlot)
            }
            startActivity(intent)
        }
    }

    private fun initializeAppointmentsTable() {
        database.reference.child("appointments").get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    // Créer un rendez-vous fictif qui sera supprimé plus tard
                    val dummyAppointment = Appointment(
                        id = "dummy",
                        doctorId = "dummy",
                        patientId = "dummy",
                        date = 0L,
                        timeSlot = "00:00",
                        status = "dummy"
                    )
                    database.reference.child("appointments")
                        .child("dummy")
                        .setValue(dummyAppointment)
                        .addOnSuccessListener {
                            // Supprimer immédiatement le rendez-vous fictif
                            database.reference.child("appointments")
                                .child("dummy")
                                .removeValue()
                        }
                }
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