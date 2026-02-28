package com.rifqidev.x_posetracker.ui.record_supervisor

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.adapter.EventAdapter
import com.rifqidev.x_posetracker.databinding.ActivitySupervisorRecordBinding
import com.rifqidev.x_posetracker.ui.add_activity.AddEventActivity

class SupervisorRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupervisorRecordBinding

    private lateinit var activityViewModel: SupervisorRecordViewModel
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySupervisorRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this.application)
        activityViewModel = ViewModelProvider(this, factory)[SupervisorRecordViewModel::class.java]

        adapter = EventAdapter()
        binding.rvActivity.adapter = adapter

        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvActivity.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvActivity.layoutManager = LinearLayoutManager(this)
        }

        activityViewModel.allActivities.observe(this) { activityList ->
            if (activityList.isNullOrEmpty()) {
                binding.noData.visibility = View.VISIBLE
                adapter.setData(emptyList())
            } else {
                binding.noData.visibility = View.GONE
                adapter.setData(activityList)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addButton.setOnClickListener {
            val intent = Intent(
                this,
                AddEventActivity::class.java
            )
            startActivity(intent)
        }

        supportActionBar?.hide()
    }
}