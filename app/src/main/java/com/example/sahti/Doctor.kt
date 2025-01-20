package com.example.sahti

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DoctorModel(
    val id: String,
    val name: String,
    val speciality: String,
    val address: String,
    val photoUrl: String?
) : Parcelable 