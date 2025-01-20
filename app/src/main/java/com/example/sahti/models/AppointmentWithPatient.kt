package com.example.sahti.models

data class AppointmentWithPatient(
    val id: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val date: Long = 0L,
    val timeSlot: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
) 