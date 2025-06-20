package com.rifqidev.x_posetracker.ui.result

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.adapter.ListWorkoutAdapter
import com.rifqidev.x_posetracker.data.WorkoutItem
import com.rifqidev.x_posetracker.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val names = resources.getStringArray(R.array.categories_menu)

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

        binding.continueButton.setOnClickListener {
            finish()
        }

        val adapter = ListWorkoutAdapter(workoutItems)
        binding.rvWorkout.adapter = adapter
    }

    private fun showConfirmationDialog(message: Int, type: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.yes) { _, _ ->
            if (type == 0) {
                finish()
            }
        }
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showConfirmationDialog(R.string.cancel_activity_confirmation, 0)
    }
}