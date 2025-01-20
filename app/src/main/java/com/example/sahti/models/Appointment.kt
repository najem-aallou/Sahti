package com.example.sahti.models

data class Appointment(
    var id: String = "",
    var doctorId: String = "",
    var patientId: String = "",
    var date: Long = 0L,
    var timeSlot: String = "",
    var status: String = "pending", // pending, validated, rejected
    var createdAt: Long = System.currentTimeMillis()
) {
    // Constructeur sans arguments requis pour Firebase
    constructor() : this("", "", "", 0L, "", "pending", System.currentTimeMillis())
} 