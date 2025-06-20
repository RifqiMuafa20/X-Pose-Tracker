package com.rifqidev.x_posetracker.ui.welcome

import android.app.DatePickerDialog
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
import com.rifqidev.x_posetracker.databinding.FragmentStepOneBinding
import java.util.Calendar

class StepOneFragment : Fragment() {

    private var _binding: FragmentStepOneBinding? = null
    private val binding get() = _binding

    private lateinit var viewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStepOneBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), factory)[WelcomeViewModel::class.java]

        viewModel.name.observe(viewLifecycleOwner) { name ->
            if (binding?.nameInput?.text.toString() != name) {
                binding?.nameInput?.setText(name)
            }
        }

        binding?.nameInput?.addTextChangedListener {
            viewModel.setName(it.toString())
        }

        binding?.materialCardView?.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(requireActivity(), { _, year, month, day ->
                    val selectedDate = String.format("%d/%d/%d", day, month + 1, year)
                    binding?.dateText?.text = selectedDate

                    viewModel.setBirthDate(selectedDate)
                }, year, month, day)

            datePickerDialog.show()
        }

        binding?.genderToggleGroup?.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_male -> {
                        viewModel.setGender(getString(R.string.pria))
                    }

                    R.id.btn_female -> {
                        viewModel.setGender(getString(R.string.wanita))
                    }
                }
            }
        }

        viewModel.birthDate.observe(viewLifecycleOwner) {
            binding?.dateText?.text = it
        }

        viewModel.gender.observe(viewLifecycleOwner) { gender ->
            when (gender) {
                getString(R.string.pria) -> {
                    if (binding?.genderToggleGroup?.checkedButtonId != R.id.btn_male) {
                        binding?.genderToggleGroup?.check(R.id.btn_male)
                    }
                }

                getString(R.string.wanita) -> {
                    if (binding?.genderToggleGroup?.checkedButtonId != R.id.btn_female) {
                        binding?.genderToggleGroup?.check(R.id.btn_female)
                    }
                }
            }
        }

        binding?.continueButton?.setOnClickListener {
            when {
                binding?.nameInput?.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.name_empty))
                }

                binding?.dateText?.text == getString(R.string.tanggal_lahir) -> {
                    showToast(getString(R.string.date_empty))
                }

                binding?.genderToggleGroup?.checkedButtonId == -1 -> {
                    showToast(getString(R.string.gender_empty))
                }

                else -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, StepTwoFragment())
                        .addToBackStack(null).commit()
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