package com.example.sahti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.R

class TimeSlotsAdapter(private val onTimeSlotSelected: (String) -> Unit) : 
    ListAdapter<String, TimeSlotsAdapter.TimeSlotViewHolder>(TimeSlotDiffCallback()) {
    
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView as TextView

        fun bind(timeSlot: String) {
            textView.text = timeSlot
            textView.isSelected = adapterPosition == selectedPosition

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onTimeSlotSelected(timeSlot)
            }
        }
    }
}

class TimeSlotDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
} 