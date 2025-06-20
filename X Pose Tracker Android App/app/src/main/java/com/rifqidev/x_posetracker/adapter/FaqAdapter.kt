package com.rifqidev.x_posetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.FaqItem

class FaqAdapter(private val faqList: List<FaqItem>) :
    RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    inner class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val answerText: TextView = itemView.findViewById(R.id.answerText)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
        val questionLayout: LinearLayout = itemView.findViewById(R.id.questionLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val item = faqList[position]
        holder.questionText.text = item.question
        holder.answerText.text = item.answer
        holder.answerText.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

        holder.arrowIcon.rotation = if (item.isExpanded) 180f else 0f

        holder.questionLayout.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = faqList.size
}
