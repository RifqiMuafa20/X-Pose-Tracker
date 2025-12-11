package com.rifqidev.x_posetracker.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.UserRecordEntity
import com.rifqidev.x_posetracker.ui.result.ResultActivity
import com.rifqidev.x_posetracker.utils.DateHelper
import com.rifqidev.x_posetracker.utils.toBitmap

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ActivityViewHolder>() {

    private val items = mutableListOf<UserRecordEntity>()

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.item_name)
        val date: TextView = itemView.findViewById(R.id.item_date)
        val time: TextView = itemView.findViewById(R.id.item_time)
        val image: ImageView = itemView.findViewById(R.id.frame_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.recordName
        holder.date.text = item.recordDate?.let { DateHelper.formatDateToIndo(it) }
        holder.time.text = item.recordTime
        holder.image.setImageBitmap(item.recordPhotos?.toBitmap())

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ResultActivity::class.java)
            intent.putExtra("record_id", item.idRecord)
            intent.putExtra("date", item.recordDate)
            intent.putExtra("time", item.recordTime)
            intent.putExtra("duration", item.recordDuration)
            intent.putExtra("calorie", item.recordCalories)
            intent.putExtra("push_up", item.pushupCount)
            intent.putExtra("sit_up", item.situpCount)
            intent.putExtra("pull_up", item.pullupCount)
            intent.putExtra("lunges", item.lungesCount)
            intent.putExtra("record_type", 2)
            intent.putExtra("record_photo_bytes", item.recordPhotos)

            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setData(newList: List<UserRecordEntity>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
