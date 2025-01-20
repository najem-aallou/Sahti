package com.example.sahti.models

data class AppointmentWithDoctor(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorSpeciality: String = "",
    val patientId: String = "",
    val date: Long = 0L,
    val timeSlot: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
) 