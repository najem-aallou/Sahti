package com.example.sahti.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sahti.R
import java.text.SimpleDateFormat
import java.util.*

class DaysAdapter(private val onDaySelected: (Calendar) -> Unit) : 
    ListAdapter<Calendar, DaysAdapter.DayViewHolder>(DayDiffCallback()) {
    
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView as TextView

        fun bind(day: Calendar) {
            val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())
            textView.text = dateFormat.format(day.time)
            textView.isSelected = adapterPosition == selectedPosition

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onDaySelected(day)
            }
        }
    }
}

class DayDiffCallback : DiffUtil.ItemCallback<Calendar>() {
    override fun areItemsTheSame(oldItem: Calendar, newItem: Calendar): Boolean {
        return oldItem.timeInMillis == newItem.timeInMillis
    }

    override fun areContentsTheSame(oldItem: Calendar, newItem: Calendar): Boolean {
        return oldItem.timeInMillis == newItem.timeInMillis
    }
} 