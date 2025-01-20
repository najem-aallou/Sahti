package com.example.sahti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.R
import com.example.sahti.models.AppointmentWithDoctor
import java.text.SimpleDateFormat
import java.util.*

class PatientAppointmentsAdapter : RecyclerView.Adapter<PatientAppointmentsAdapter.AppointmentViewHolder>() {
    private var appointments = listOf<AppointmentWithDoctor>()

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val doctorNameText: TextView = view.findViewById(R.id.doctorNameText)
        val specialityText: TextView = view.findViewById(R.id.specialityText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val statusText: TextView = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        
        holder.doctorNameText.text = "Dr. ${appointment.doctorName}"
        holder.specialityText.text = appointment.doctorSpeciality
        holder.dateText.text = "Date : ${dateFormat.format(Date(appointment.date))}"
        holder.timeText.text = "Heure : ${appointment.timeSlot}"
        
        val status = when (appointment.status) {
            "pending" -> "En attente de confirmation"
            "validated" -> "Confirmé"
            "rejected" -> "Refusé"
            else -> appointment.status
        }
        holder.statusText.text = "Statut : $status"
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<AppointmentWithDoctor>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
} 