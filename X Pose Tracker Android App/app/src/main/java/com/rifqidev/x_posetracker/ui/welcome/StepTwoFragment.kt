package com.rifqidev.x_posetracker.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.FragmentStepTwoBinding

class StepTwoFragment : Fragment() {
    private var _binding: FragmentStepTwoBinding? = null
    private val binding get() = _binding

    private lateinit var viewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStepTwoBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory)[WelcomeViewModel::class.java]

        viewModel.weight.observe(viewLifecycleOwner) { weight ->
            if (binding?.weightInput?.text.toString() != weight) {
                binding?.weightInput?.setText(weight)
            }
        }

        viewModel.height.observe(viewLifecycleOwner) { height ->
            if (binding?.heightInput?.text.toString() != height) {
                binding?.heightInput?.setText(height)
            }
        }

        binding?.weightInput?.addTextChangedListener {
            viewModel.setWeight(it.toString())
        }

        binding?.heightInput?.addTextChangedListener {
            viewModel.setHeight(it.toString())
        }

        binding?.continueButton?.setOnClickListener {
            when {
                binding?.weightInput?.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.weight_empty))
                }

                binding?.heightInput?.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.height_empty))
                }

                else -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, StepThreeFragment())
                        .addToBackStack(null).commit()
                }
            }
        }

        binding?.backButton?.setOnClickListener {
            activity?.onBackPressed()
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