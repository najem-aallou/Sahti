package com.example.sahti.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.DoctorModel
import com.example.sahti.DoctorDetailActivity
import com.example.sahti.R

class DoctorsAdapter : RecyclerView.Adapter<DoctorsAdapter.DoctorViewHolder>() {
    private var doctors = listOf<DoctorModel>()

    fun updateDoctors(newDoctors: List<DoctorModel>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount() = doctors.size

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.doctorName)
        private val specialityText: TextView = itemView.findViewById(R.id.doctorSpeciality)
        private val addressText: TextView = itemView.findViewById(R.id.doctorAddress)
        private val imageView: ImageView = itemView.findViewById(R.id.doctorImage)
        private val btnDetails: View = itemView.findViewById(R.id.btnDetails)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val doctor = doctors[position]
                    val context = itemView.context
                    val intent = Intent(context, DoctorDetailActivity::class.java).apply {
                        putExtra("DOCTOR_ID", doctor.id)
                        putExtra("DOCTOR_NAME", doctor.name)
                        putExtra("DOCTOR_SPECIALITY", doctor.speciality)
                        putExtra("DOCTOR_PHOTO", doctor.photoUrl)
                    }
                    context.startActivity(intent)
                }
            }

            btnDetails.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val doctor = doctors[position]
                    val context = itemView.context
                    val intent = Intent(context, DoctorDetailActivity::class.java)
                    intent.putExtra("DOCTOR_ID", doctor.id)
                    context.startActivity(intent)
                }
            }
        }

        fun bind(doctor: DoctorModel) {
            nameText.text = doctor.name
            specialityText.text = doctor.speciality
            addressText.text = doctor.address

            // Charger l'image de profil si disponible
            doctor.photoUrl?.let { imageData ->
                try {
                    when {
                        imageData.startsWith("data:image") -> {
                            val base64Image = imageData.substringAfter("base64,")
                            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        else -> {
                            imageView.setImageResource(R.drawable.default_profile)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DoctorViewHolder", "Erreur lors du chargement de l'image: ${e.message}")
                    imageView.setImageResource(R.drawable.default_profile)
                }
            } ?: imageView.setImageResource(R.drawable.default_profile)
        }
    }
} 