package com.rifqidev.x_posetracker.ui.record_private

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.ActivityPrivateRecordBinding
import com.rifqidev.x_posetracker.ui.camera.CameraActivity

class PrivateRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivateRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPrivateRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val recordType = intent.getIntExtra("record_type", 0)
        val memberId = intent.getStringExtra("member_id") ?: ""

        val menuItems = resources.getStringArray(R.array.category_menu)
        binding.categoryOption.setText(menuItems[0], false)

        binding.continueButton.setOnClickListener {
            when {
                binding.durationInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.duration_empty))
                }

                binding.categoryOption.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.category_empty))
                }

                else -> {
                    showToast(getString(R.string.start_recording))

                    val activityType = binding.categoryOption.text.toString()
                    val durationText = binding.durationInput.text.toString()
                    val duration = durationText.toLongOrNull() ?: 0L

                    val intent = Intent(this, CameraActivity::class.java)
                    intent.putExtra("type", activityType)
                    intent.putExtra("duration", duration)
                    intent.putExtra("member_id", memberId)
                    intent.putExtra("record_type", recordType)
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}