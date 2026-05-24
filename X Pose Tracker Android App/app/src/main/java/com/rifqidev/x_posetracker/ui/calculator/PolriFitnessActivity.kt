package com.rifqidev.x_posetracker.ui.calculator

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.ActivityPolriFitnessBinding

class PolriFitnessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPolriFitnessBinding
    private lateinit var viewModel: CalculatorPolriViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPolriFitnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val factory = ViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory)[CalculatorPolriViewModel::class.java]

        setupGender()
        setupInput()
        observeData()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.submitButton.setOnClickListener {
            binding.resultCard.visibility = View.VISIBLE

            viewModel.calculateFitnessBResult()
            viewModel.calculateFinalResult()

            binding.fitnessAResult.text = getString(R.string.fitness_a_result, viewModel.runningPolri.value?.toDouble())
            binding.fitnessBResult.text = getString(R.string.fitness_b_result, viewModel.fitnessBResult.value?.toDouble())
            binding.fitnessCResult.text = getString(R.string.fitness_c_result, viewModel.renangPolri.value?.toDouble())
            binding.finalResult.text = getString(R.string.final_result, viewModel.finalResult.value?.toDouble())
        }
    }

    private fun setupGender() {
        val menuItems = resources.getStringArray(R.array.gender_items)
        binding.genderOption.setText(menuItems[0], false)

        viewModel.setGender(0)

        binding.genderOption.setOnItemClickListener { _, _, position, _ ->
            viewModel.setGender(position)
            calculateRunning()
            calculatePullUp()
            calculateSitUp()
            calculatePushUp()
            calculateShuttleRun()
            calculateSwimming()
        }
    }

    private fun setupInput() {
        binding.runningInput.doAfterTextChanged {
            calculateRunning()
        }

        binding.pullUpInput.doAfterTextChanged {
            calculatePullUp()
        }

        binding.sitUpInput.doAfterTextChanged {
            calculateSitUp()
        }

        binding.pushUpInput.doAfterTextChanged {
            calculatePushUp()
        }

        binding.shuttleRunInput.doAfterTextChanged {
            calculateShuttleRun()
        }

        binding.swimmingInput.doAfterTextChanged {
            calculateSwimming()
        }
    }

    private fun calculateRunning() {
        val distance =
            binding.runningInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculateRunningPolri(distance)
    }

    private fun calculatePullUp() {
        val count =
            binding.pullUpInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculatePullUpPolri(count)
    }

    private fun calculatePushUp() {
        val count =
            binding.pushUpInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculatePushUpPolri(count)
    }

    private fun calculateSitUp() {
        val count =
            binding.sitUpInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculateSitUpPolri(count)
    }

    private fun calculateShuttleRun() {
        val count =
            binding.shuttleRunInput.text.toString()

        val time: Double? = count.replace(":", ".").toDoubleOrNull()

        if (time != null) {
            viewModel.calculateShuttleRunPolri(time)
        } else {
            viewModel.calculateShuttleRunPolri(100.0)
        }
    }

    private fun calculateSwimming() {
        val count =
            binding.swimmingInput.text.toString()

        val time: Double? = count.replace(":", ".").toDoubleOrNull()

        if (time != null) {
            viewModel.calculateRenangPolri(time)
        } else {
            viewModel.calculateRenangPolri(100.0)
        }
    }

    private fun observeData() {
        viewModel.runningPolri.observe(this) {
            binding.runningResult.text = it.toString()
        }

        viewModel.pullUpPolri.observe(this) {
            binding.pullUpResult.text = it.toString()
        }

        viewModel.sitUpPolri.observe(this) {
            binding.sitUpResult.text = it.toString()
        }

        viewModel.pushUpPolri.observe(this) {
            binding.pushUpResult.text = it.toString()
        }

        viewModel.shuttleRunPolri.observe(this) {
            binding.shuttleRunResult.text = it.toString()
        }

        viewModel.renangPolri.observe(this) {
            binding.swimmingResult.text = it.toString()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}