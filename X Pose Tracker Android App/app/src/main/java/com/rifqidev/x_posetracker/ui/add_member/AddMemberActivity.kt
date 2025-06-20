package com.rifqidev.x_posetracker.ui.add_member

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.ActivityMemberEntity
import com.rifqidev.x_posetracker.databinding.ActivityAddMemberBinding
import java.util.UUID

class AddMemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMemberBinding

    private lateinit var viewModel: AddMemberViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddMemberViewModel::class.java]

        supportActionBar?.hide()

        val activityId = intent.getStringExtra("activity_id")

        binding.continueButton.setOnClickListener {
            when {
                binding.nameInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.name_empty))
                }

                binding.registrationInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.regis_empty))
                }

                else -> {
                    showToast(getString(R.string.member_added))
                    val member = ActivityMemberEntity(
                        idActivity = activityId!!,
                        idMember = UUID.randomUUID().toString(),
                        memberName = binding.nameInput.text.toString(),
                        memberRegistrationNumber = binding.registrationInput.text.toString()
                    )

                    viewModel.insertActivityMember(member)
                    finish()
                }
            }
        }

        binding.backButton.setOnClickListener {
            showConfirmationDialog(R.string.cancel_confirmation, 0)
        }
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