package com.rifqidev.x_posetracker.ui.detail_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.adapter.MemberAdapter
import com.rifqidev.x_posetracker.databinding.ActivityDetailEventBinding
import com.rifqidev.x_posetracker.ui.add_member.AddMemberActivity
import com.rifqidev.x_posetracker.ui.edit_activity.EditEventActivity
import com.rifqidev.x_posetracker.utils.DateHelper

class DetailEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailEventBinding

    private lateinit var viewModel: DetailEventViewModel

    private lateinit var adapter: MemberAdapter

    private var activityId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, viewModelFactory)[DetailEventViewModel::class.java]

        supportActionBar?.hide()

        adapter = MemberAdapter { member ->
            viewModel.deleteMemberById(member.idMember, member.idActivity)
        }

        binding.rvMember.adapter = adapter
        binding.rvMember.layoutManager = LinearLayoutManager(this)

        activityId = intent.getStringExtra("activity_id")

        if (activityId != null) {
            viewModel.getActivityById(activityId!!).observe(this) { activity ->
                if (activity != null) {
                    binding.activityName.text = activity.activityName
                    binding.activityDate.text = activity.activityDate?.let {
                        DateHelper.formatDateToIndo(
                            it
                        )
                    }
                    binding.activityLocation.text = activity.activityLocation
                    binding.activitySupervisor.text = activity.activitySupervisor
                    binding.activityCount.text = "${activity.memberAmount} orang"
                }
            }

            viewModel.getActivityMembers(activityId!!).observe(this) { memberList ->
                if (memberList.isNullOrEmpty()) {
                    binding.noData.visibility = View.VISIBLE
                    adapter.setData(emptyList())
                } else {
                    binding.noData.visibility = View.GONE
                    adapter.setData(memberList)
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteActivityById(activityId!!)

            showToast(getString(R.string.deleted))
            finish()
        }

        binding.addMember.setOnClickListener {
            val intent = Intent(this, AddMemberActivity::class.java)
            intent.putExtra("activity_id", activityId)
            startActivity(intent)
        }

        binding.editActivity.setOnClickListener {
            val intent = Intent(this, EditEventActivity::class.java)
            intent.putExtra("activity_id", activityId)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}