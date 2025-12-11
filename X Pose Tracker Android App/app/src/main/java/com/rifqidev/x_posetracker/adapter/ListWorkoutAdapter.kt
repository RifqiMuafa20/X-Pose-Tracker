package com.rifqidev.x_posetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.WorkoutItem
import com.rifqidev.x_posetracker.utils.DateHelper

class ListWorkoutAdapter(private val items: List<WorkoutItem>) :
    RecyclerView.Adapter<ListWorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.workout_icon)
        val name: TextView = itemView.findViewById(R.id.workout_name)
        val date: TextView = itemView.findViewById(R.id.workout_date)
        val repetition: TextView = itemView.findViewById(R.id.workout_repetition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_item, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconResId)
        holder.name.text = item.name
        holder.date.text = item.date?.let { DateHelper.formatDateToIndo(it) } ?: ""
        holder.repetition.text = "${item.repetition} X"
    }

    override fun getItemCount(): Int = items.size
}