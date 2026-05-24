package com.rifqidev.x_posetracker.ui.calculator

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.ActivityTniFitnessBinding

class TniFitnessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTniFitnessBinding
    private lateinit var viewModel: CalculatorTniViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTniFitnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val factory = ViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory)[CalculatorTniViewModel::class.java]

        setupGender()
        setupAge()
        setupInput()
        observeData()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.submitButton.setOnClickListener {
            binding.resultCard.visibility = View.VISIBLE

            viewModel.calculateFitnessBResult()
            viewModel.calculateFinalResult()

            binding.fitnessAResult.text = getString(R.string.fitness_a_result, viewModel.runningTni.value?.toDouble())
            binding.fitnessBResult.text = getString(R.string.fitness_b_result, viewModel.fitnessBResult.value?.toDouble())
            binding.fitnessCResult.text = getString(R.string.fitness_c_result, viewModel.swimmingTni.value?.toDouble())
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

    private fun setupAge() {
        binding.ageInput.doAfterTextChanged { text ->
            val age = text.toString().toIntOrNull() ?: 0

            viewModel.setAge(age)

            if (age > 0) {
                calculateRunning()
                calculatePullUp()
                calculateSitUp()
                calculatePushUp()
                calculateShuttleRun()
                calculateSwimming()
            } else {
                viewModel.setAge(0)
            }
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

        viewModel.calculateRunningTni(distance)
    }

    private fun calculatePullUp() {
        val count =
            binding.pullUpInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculatePullUpTni(count)
    }

    private fun calculateSitUp() {
        val count =
            binding.sitUpInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculateSitUpTni(count)
    }

    private fun calculatePushUp() {
        val count =
            binding.pushUpInput.text.toString()
                .toIntOrNull() ?: 0

        viewModel.calculatePushUpTni(count)
    }

    private fun calculateShuttleRun() {
        val count =
            binding.shuttleRunInput.text.toString()

        val time: Double? = count.replace(":", ".").toDoubleOrNull()

        if (time != null) {
            viewModel.calculateShuttleRunTni(time)
        } else {
            viewModel.calculateShuttleRunTni(100.0)
        }
    }

    private fun calculateSwimming() {
        val count =
            binding.swimmingInput.text.toString()
                .toIntOrNull() ?: 240

        viewModel.calculateSwimmingTni(count)
    }

    private fun observeData() {
        viewModel.runningTni.observe(this) {
            binding.runningResult.text = it.toString()
        }

        viewModel.pullUpTni.observe(this) {
            binding.pullUpResult.text = it.toString()
        }

        viewModel.sitUpTni.observe(this) {
            binding.sitUpResult.text = it.toString()
        }

        viewModel.pushUpTni.observe(this) {
            binding.pushUpResult.text = it.toString()
        }

        viewModel.shuttleRunTni.observe(this) {
            binding.shuttleRunResult.text = it.toString()
        }

        viewModel.swimmingTni.observe(this) {
            binding.swimmingResult.text = it.toString()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}