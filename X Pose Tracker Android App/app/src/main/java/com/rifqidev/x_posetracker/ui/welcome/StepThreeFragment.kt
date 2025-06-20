package com.rifqidev.x_posetracker.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.data.UserProfileEntity
import com.rifqidev.x_posetracker.databinding.FragmentStepThreeBinding
import com.rifqidev.x_posetracker.ui.MainActivity
import java.util.UUID

class StepThreeFragment : Fragment() {
    private var _binding: FragmentStepThreeBinding? = null
    private val binding get() = _binding

    private lateinit var viewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStepThreeBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory)[WelcomeViewModel::class.java]

        binding?.goalToggleGroup?.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.option1 -> {
                        viewModel.setGoal(getString(R.string.option1))
                    }

                    R.id.option2 -> {
                        viewModel.setGoal(getString(R.string.option2))
                    }

                    R.id.option3 -> {
                        viewModel.setGoal(getString(R.string.option3))
                    }
                }
            }
        }

        viewModel.goal.observe(viewLifecycleOwner) { goal ->
            when (goal) {
                getString(R.string.option1) -> {
                    if (binding?.goalToggleGroup?.checkedButtonId != R.id.option1) {
                        binding?.goalToggleGroup?.check(R.id.option1)
                    }
                }

                getString(R.string.option2) -> {
                    if (binding?.goalToggleGroup?.checkedButtonId != R.id.option2) {
                        binding?.goalToggleGroup?.check(R.id.option2)
                    }
                }

                getString(R.string.option3) -> {
                    if (binding?.goalToggleGroup?.checkedButtonId != R.id.option3) {
                        binding?.goalToggleGroup?.check(R.id.option3)
                    }
                }
            }
        }

        binding?.backButton?.setOnClickListener {
            activity?.onBackPressed()
        }

        binding?.finishButton?.setOnClickListener {
            when {
                binding?.goalToggleGroup?.checkedButtonId == -1 -> {
                    showToast(getString(R.string.goal_empty))
                }

                else -> {
                    val name = viewModel.name.value
                    val birthDate = viewModel.birthDate.value
                    val gender = viewModel.gender.value
                    val weight = viewModel.weight.value?.toIntOrNull()
                    val height = viewModel.height.value?.toIntOrNull()
                    val goal = viewModel.goal.value

                    val user = UserProfileEntity(
                        idUser = UUID.randomUUID().toString(),
                        userName = name,
                        userBirth = birthDate,
                        userGender = gender,
                        userWeight = weight,
                        userHeight = height,
                        userGoal = goal,
                        userProfile = null,
                    )

                    viewModel.insertUserProfile(user as UserProfileEntity)
                    showToast(getString(R.string.welcome))

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}