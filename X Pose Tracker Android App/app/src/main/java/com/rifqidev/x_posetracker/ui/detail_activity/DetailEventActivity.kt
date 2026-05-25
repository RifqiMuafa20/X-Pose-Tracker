package com.rifqidev.x_posetracker.ui.detail_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ContentValues
import android.content.res.Configuration
import android.provider.MediaStore
import android.os.Environment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.flow.first
import java.io.OutputStreamWriter

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

        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvMember.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvMember.layoutManager = LinearLayoutManager(this)
        }

        activityId = intent.getStringExtra("activity_id")

        if (activityId != null) {
            lifecycleScope.launch {

                viewModel.getActivityById(activityId!!)
                    .collect { activity ->

                        if (activity == null) {
                            showToast(getString(R.string.deleted))

                            finish()

                            return@collect
                        }

                        binding.activityName.text =
                            activity.activityName

                        binding.activityDate.text =
                            activity.activityDate?.let { date ->
                                DateHelper.formatDateToIndo(date)
                            }

                        binding.activityLocation.text =
                            activity.activityLocation

                        binding.activitySupervisor.text =
                            activity.activitySupervisor

                        binding.activityCount.text =
                            getString(
                                R.string.person_format,
                                activity.memberAmount
                            )
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
            showConfirmationDialog((R.string.delete_event_confirmation), 0)
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

        binding.exportButton.setOnClickListener {
            lifecycleScope.launch {
                showLoading(true)

                try {
                    val activityDetail =
                        viewModel.getActivityById(activityId!!).first()
                    val memberList = viewModel.getDetailActivityMemberRecord(activityId.toString())

                    withContext(Dispatchers.IO) {

                        val fileName = "Activity_${activityId}.csv"

                        val resolver = contentResolver

                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }

                        val uri = resolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            contentValues
                        ) ?: throw Exception(getString(R.string.failed_create_file))

                        resolver.openOutputStream(uri)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->

                                // ACTIVITY
                                writer.append("Activity Name,Date,Supervisor,Location,Total Members\n")
                                writer.append(
                                    "${activityDetail.activityName ?: ""}," +
                                            "${activityDetail.activityDate ?: ""}," +
                                            "${activityDetail.activitySupervisor ?: ""}," +
                                            "${activityDetail.activityLocation ?: ""}," +
                                            "${activityDetail.memberAmount ?: 0}\n\n"
                                )

                                // MEMBER
                                writer.append("Name,Registration Number,Push-up,Pull-up,Sit-up,Lunges\n")

                                memberList.forEach { member ->
                                    writer.append(
                                        "${member.memberName ?: ""}," +
                                                "${member.memberRegistrationNumber ?: ""}," +
                                                "${member.maxPushup ?: 0}," +
                                                "${member.maxPullup ?: 0}," +
                                                "${member.maxSitup ?: 0}," +
                                                "${member.maxLunges ?: 0}\n"
                                    )
                                }

                                writer.flush()
                            }
                        }

                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "text/csv")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }

                        startActivity(Intent.createChooser(openIntent, getString(R.string.open_csv)))
                    }

                    showToast(getString(R.string.csv_success))

                } catch (e: Exception) {
                    showToast("Failed to export: ${e.message}")
                }

                showLoading(false)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun showConfirmationDialog(message: Int, type: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.yes) { _, _ ->
            if (type == 0) {
                viewModel.deleteActivityById(activityId!!)
            }
        }
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}