package com.example.sahti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.R
import com.example.sahti.models.AppointmentWithPatient
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class DoctorAppointmentsAdapter(
    private val onValidateClick: (AppointmentWithPatient) -> Unit,
    private val onRejectClick: (AppointmentWithPatient) -> Unit
) : RecyclerView.Adapter<DoctorAppointmentsAdapter.AppointmentViewHolder>() {
    
    private var appointments = listOf<AppointmentWithPatient>()

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val patientNameText: TextView = view.findViewById(R.id.patientNameText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val validateButton: MaterialButton = view.findViewById(R.id.validateButton)
        val rejectButton: MaterialButton = view.findViewById(R.id.rejectButton)
        val statusText: TextView = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        
        holder.patientNameText.text = "Patient : ${appointment.patientName}"
        holder.dateText.text = "Date : ${dateFormat.format(Date(appointment.date))}"
        holder.timeText.text = "Heure : ${appointment.timeSlot}"
        
        val status = when (appointment.status) {
            "pending" -> "En attente"
            "validated" -> "Validé"
            "rejected" -> "Refusé"
            else -> appointment.status
        }
        holder.statusText.text = "Statut : $status"
        
        if (appointment.status == "pending") {
            holder.validateButton.visibility = View.VISIBLE
            holder.rejectButton.visibility = View.VISIBLE
        } else {
            holder.validateButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
        }
        
        holder.validateButton.setOnClickListener { onValidateClick(appointment) }
        holder.rejectButton.setOnClickListener { onRejectClick(appointment) }
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<AppointmentWithPatient>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
} 