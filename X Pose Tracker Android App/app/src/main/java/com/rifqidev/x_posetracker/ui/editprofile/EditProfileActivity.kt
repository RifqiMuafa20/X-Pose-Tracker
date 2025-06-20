package com.rifqidev.x_posetracker.ui.editprofile

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.databinding.ActivityEditProfileBinding
import com.rifqidev.x_posetracker.utils.getImageUri
import com.rifqidev.x_posetracker.utils.reduceFileImage
import com.rifqidev.x_posetracker.utils.toBitmap
import com.rifqidev.x_posetracker.utils.uriToFile
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var viewModel: EditProfileViewModel

    private var id: String? = null
    private var goal: String? = null

    private var currentImageUri: Uri? = null
    private val REQUEST_CODE_CHOOSE_IMAGE = 100
    private var reducedImageByteArray: ByteArray? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[EditProfileViewModel::class.java]

        viewModel.getUserProfile().observe(this) { userProfile ->
            if (userProfile != null) {
                binding.fullname.text = userProfile.userName
                id = userProfile.idUser
                goal = userProfile.userGoal
                binding.nameInput.setText(userProfile.userName)
                binding.dateText.text = userProfile.userBirth
                when (userProfile.userGender) {
                    getString(R.string.pria) -> {
                        if (binding.genderToggleGroup.checkedButtonId != R.id.btn_male) {
                            binding.genderToggleGroup.check(R.id.btn_male)
                        }
                    }

                    getString(R.string.wanita) -> {
                        if (binding.genderToggleGroup.checkedButtonId != R.id.btn_female) {
                            binding.genderToggleGroup.check(R.id.btn_female)
                        }
                    }
                }
                binding.heightInput.setText(userProfile.userHeight.toString())
                binding.weightInput.setText(userProfile.userWeight.toString())
                val profileImage = userProfile.userProfile?.toBitmap()
                if (userProfile.userProfile != null) {
                    binding.profileImage.setImageBitmap(profileImage)
                    reducedImageByteArray = userProfile.userProfile
                } else {
                    binding.profileImage.setImageResource(R.drawable.account_icon)
                }
            }
        }

        binding.materialCardView.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(this, { _, year, month, day ->
                    val selectedDate = String.format("%d/%d/%d", day, month + 1, year)
                    binding.dateText.text = selectedDate

                }, year, month, day)

            datePickerDialog.show()
        }

        binding.updateButton.setOnClickListener {
            when {
                binding.nameInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.name_empty))
                }

                binding.dateText.text == getString(R.string.tanggal_lahir) -> {
                    showToast(getString(R.string.date_empty))
                }

                binding.genderToggleGroup.checkedButtonId == -1 -> {
                    showToast(getString(R.string.gender_empty))
                }

                binding.weightInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.weight_empty))
                }

                binding.heightInput.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.height_empty))
                }

                else -> {
                    updateProfile()
                }
            }
        }

        binding.backButton.setOnClickListener {
            showConfirmationDialog(R.string.cancel_confirmation, 0)
        }

        binding.cameraIcon.setOnClickListener {
            openCameraOrGalleryChooser(this)
        }
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

    private fun updateProfile() {
        val userName = binding.nameInput.text.toString()
        val userBirth = binding.dateText.text.toString()
        val userGender = when (binding.genderToggleGroup.checkedButtonId) {
            R.id.btn_male -> getString(R.string.pria)
            R.id.btn_female -> getString(R.string.wanita)
            else -> getString(R.string.pria)
        }
        val userHeight = binding.heightInput.text.toString().toInt()
        val userWeight = binding.weightInput.text.toString().toInt()

        if (currentImageUri != null) {
            val imageFile = uriToFile(currentImageUri!!, this).reduceFileImage()
            reducedImageByteArray = imageFile.readBytes()
        }

        val userProfile = UserProfileEntity(
            idUser = id!!,
            userName = userName,
            userBirth = userBirth,
            userGender = userGender,
            userHeight = userHeight,
            userWeight = userWeight,
            userGoal = goal,
            userProfile = reducedImageByteArray
        )

        viewModel.updateUserProfile(userProfile)
        showToast(getString(R.string.profile_updated))
        finish()
    }

    private fun openCameraOrGalleryChooser(context: Context) {
        val imageUri = getImageUri(context)
        currentImageUri = imageUri

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }

        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        val chooser = Intent.createChooser(galleryIntent, "Pilih Sumber Gambar")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooser, REQUEST_CODE_CHOOSE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_IMAGE) {
            val selectedImageUri = data?.data ?: currentImageUri
            selectedImageUri?.let {
                currentImageUri = it
                binding.profileImage.setImageURI(it)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentImageUri", currentImageUri?.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showConfirmationDialog(R.string.cancel_confirmation, 0)
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}