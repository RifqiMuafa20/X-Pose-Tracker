package com.rifqidev.x_posetracker.ui.edit_activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.ActivityEntity
import com.rifqidev.x_posetracker.databinding.ActivityAddEventBinding
import com.rifqidev.x_posetracker.utils.DateHelper
import kotlinx.coroutines.launch

class EditEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEventBinding

    private lateinit var viewModel: EditEventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, viewModelFactory)[EditEventViewModel::class.java]

        val activityId = intent.getStringExtra("activity_id")
        var activityDate: String? = null
        var member: Int? = null

        if (activityId != null) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {

                    viewModel.getActivityById(activityId)
                        .collect { activity ->

                            activity.let {

                                binding.nameInput.setText(it.activityName.orEmpty())
                                binding.locationInput.setText(it.activityLocation.orEmpty())
                                binding.supervisorInput.setText(it.activitySupervisor.orEmpty())

                                activityDate = it.activityDate.orEmpty()
                                member = it.memberAmount
                            }
                        }
                }
            }
        }

        binding.recordTitle.text = getString(R.string.edit_event)

        binding.backButton.setOnClickListener {
            showConfirmationDialog(R.string.cancel_confirmation, 0)
        }

        binding.continueButton.setOnClickListener {
            when {
                binding.nameInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.event_empty))
                }

                binding.supervisorInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.supervisor_empty))
                }

                binding.locationInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.location_empty))
                }

                else -> {
                    showToast(getString(R.string.event_edited))
                    val event = ActivityEntity(
                        idActivity = activityId!!,
                        activityName = binding.nameInput.text.toString(),
                        activityDate = activityDate ?: DateHelper.getCurrentDate(),
                        activitySupervisor = binding.supervisorInput.text.toString(),
                        activityLocation = binding.locationInput.text.toString(),
                        memberAmount = member!!
                    )

                    viewModel.updateActivity(event)
                    finish()
                }
            }
        }

        supportActionBar?.hide()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        showConfirmationDialog(R.string.cancel_confirmation, 0)
    }
}