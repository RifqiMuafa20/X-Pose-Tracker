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

        memberId = intent.getStringExtra("member_id").toString()

        viewModel.getMemberById(memberId).observe(this) { member ->
            if (member != null) {
                binding.memberName.text = member.memberName
                binding.registrationNumber.text = member.memberRegistrationNumber
            }
        }

        val names = resources.getStringArray(R.array.category_menu)

        val iconResIds = listOf(
            R.drawable.push_up_icon,
            R.drawable.sit_up_icon,
            R.drawable.pull_up_icon,
            R.drawable.lunges_icon
        )

        val workoutItems = names.mapIndexed { index, name ->
            WorkoutItem(
                name = name,
                iconResId = iconResIds[index],
                date = "-",
                repetition = "0"
            )
        }

        val adapter = ListWorkoutAdapter(workoutItems)
        binding.rvWorkout.adapter = adapter

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
            startActivity(intent)
        }

        supportActionBar?.hide()
    }
}