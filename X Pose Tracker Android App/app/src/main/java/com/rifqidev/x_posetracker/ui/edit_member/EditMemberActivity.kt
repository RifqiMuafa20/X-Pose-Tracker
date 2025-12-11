package com.rifqidev.x_posetracker.ui.edit_member

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

class EditMemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMemberBinding

    private lateinit var viewModel: EditMemberViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityAddMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[EditMemberViewModel::class.java]

        val memberId = intent.getStringExtra("member_id") ?: ""
        var activityId: String? = null

        viewModel.getMemberById(memberId).observe(this) { member ->
            if (member != null) {
                binding.nameInput.setText(member.memberName)
                binding.registrationInput.setText(member.memberRegistrationNumber)

                activityId = member.idActivity
            }
        }

        binding.continueButton.setOnClickListener {
            when {
                binding.nameInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.name_empty))
                }

                binding.registrationInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.regis_empty))
                }

                else -> {
                    val member = ActivityMemberEntity(
                        idActivity = activityId.toString(),
                        idMember = memberId,
                        memberName = binding.nameInput.text.toString(),
                        memberRegistrationNumber = binding.registrationInput.text.toString()
                    )
                    viewModel.updateMember(member)

                    showToast(getString(R.string.member_updated))
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