package com.rifqidev.x_posetracker.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.ui.detail_activity.DetailEventActivity
import com.rifqidev.x_posetracker.utils.DateHelper

class EventAdapter : RecyclerView.Adapter<EventAdapter.ActivityViewHolder>() {

    private val items = mutableListOf<ActivityEntity>()

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val date: TextView = itemView.findViewById(R.id.activity_date)
        val count: TextView = itemView.findViewById(R.id.activity_person)
        val location: TextView = itemView.findViewById(R.id.activity_location)
        val supervisor: TextView = itemView.findViewById(R.id.activity_supervisor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_item, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.activityName
        holder.date.text = item.activityDate?.let { DateHelper.formatDateToIndo(it) }
        holder.count.text = "${item.memberAmount} orang"
        holder.location.text = item.activityLocation
        holder.supervisor.text = item.activitySupervisor

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, DetailEventActivity::class.java)
            intent.putExtra("activity_id", item.idActivity)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setData(newList: List<ActivityEntity>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
