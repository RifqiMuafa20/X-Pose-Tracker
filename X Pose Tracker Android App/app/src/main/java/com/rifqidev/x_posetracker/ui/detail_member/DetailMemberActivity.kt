package com.rifqidev.x_posetracker.ui.detail_member

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.adapter.ListWorkoutAdapter
import com.rifqidev.x_posetracker.data.WorkoutItem
import com.rifqidev.x_posetracker.databinding.ActivityDetailMemberBinding
import com.rifqidev.x_posetracker.ui.camera.CameraActivity
import com.rifqidev.x_posetracker.ui.edit_member.EditMemberActivity
import com.rifqidev.x_posetracker.ui.record_private.PrivateRecordActivity
import java.util.Date

class DetailMemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailMemberBinding
    private lateinit var viewModel: DetailMemberViewModel

    var memberId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[DetailMemberViewModel::class.java]

        memberId = intent.getStringExtra("member_id") ?: ""

        viewModel.getMemberById(memberId).observe(this) { member ->
            if (member != null) {
                binding.memberName.text = member.memberName
                binding.registrationNumber.text = member.memberRegistrationNumber
            }
        }

        val names = resources.getStringArray(R.array.categories_menu)

        val iconResIds = listOf(
            R.drawable.push_up_icon,
            R.drawable.sit_up_icon,
            R.drawable.pull_up_icon,
            R.drawable.lunges_icon
        )

        val workoutItems = mutableListOf(
            WorkoutItem(names[0], iconResIds[0], date = null, repetition = "0"),
            WorkoutItem(names[1], iconResIds[1], date = null, repetition = "0"),
            WorkoutItem(names[2], iconResIds[2], date = null, repetition = "0"),
            WorkoutItem(names[3], iconResIds[3], date = null, repetition = "0")
        )

        val adapter = ListWorkoutAdapter(workoutItems)
        binding.rvWorkout.adapter = adapter

        viewModel.getTopPushUpRecordByMemberId(memberId).observe(this) { record ->
            record?.let {
                if (it.pushupCount != 0) {
                    workoutItems[0] = workoutItems[0].copy(
                        date = it.recordDate,
                        repetition = it.pushupCount.toString()
                    )
                    adapter.notifyItemChanged(0)
                }
            }
        }

        viewModel.getTopSitUpRecordByMemberId(memberId).observe(this) { record ->
            record?.let {
                if (it.situpCount != 0) {
                    workoutItems[1] = workoutItems[1].copy(
                        date = it.recordDate,
                        repetition = it.situpCount.toString()
                    )
                    adapter.notifyItemChanged(1)
                }
            }
        }

        viewModel.getTopPullUpRecordByMemberId(memberId).observe(this) { record ->
            record?.let {
                if (it.pullupCount != 0) {
                    workoutItems[2] = workoutItems[2].copy(
                        date = it.recordDate,
                        repetition = it.pullupCount.toString()
                    )
                    adapter.notifyItemChanged(2)
                }
            }
        }

        viewModel.getTopLungesRecordByMemberId(memberId).observe(this) { record ->
            record?.let {
                if (it.lungesCount != 0) {
                    workoutItems[3] = workoutItems[3].copy(
                        date = it.recordDate,
                        repetition = it.lungesCount.toString()
                    )
                    adapter.notifyItemChanged(3)
                }
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.editActivity.setOnClickListener {
            val intent = Intent(
                this,
                EditMemberActivity::class.java
            )
            intent.putExtra("member_id", memberId)
            startActivity(intent)
        }

        binding.recordActivity.setOnClickListener {
            val intent = Intent(this, PrivateRecordActivity::class.java)
            intent.putExtra("record_type", 1)
            intent.putExtra("member_id", memberId)
            startActivity(intent)
        }

        supportActionBar?.hide()
    }
}