package com.rifqidev.x_posetracker.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.ui.detail_activity.DetailEventActivity
import com.rifqidev.x_posetracker.ui.detail_member.DetailMemberActivity
import com.rifqidev.x_posetracker.utils.StringHelper

class MemberAdapter(
    private val onDeleteClick: (ActivityMemberEntity) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    private val items = mutableListOf<ActivityMemberEntity>()

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val initial: TextView = itemView.findViewById(R.id.initial_name)
        val name: TextView = itemView.findViewById(R.id.member_name)
        val regisNumber: TextView = itemView.findViewById(R.id.registration_number)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.member_item, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val item = items[position]

        holder.initial.text = item.memberName?.let { StringHelper.getInitials(it) }
        holder.name.text = item.memberName
        holder.regisNumber.text = item.memberRegistrationNumber

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, DetailMemberActivity::class.java)
            intent.putExtra("member_id", item.idMember)
            it.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setData(newList: List<ActivityMemberEntity>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
