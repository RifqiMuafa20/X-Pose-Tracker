package com.rifqidev.x_posetracker.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.mynoteapps.helper.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.FragmentProfile2Binding
import com.rifqidev.x_posetracker.ui.editprofile.EditProfileActivity
import com.rifqidev.x_posetracker.ui.faq.QuestionActivity
import com.rifqidev.x_posetracker.ui.tutorial.TutorialActivity
import com.rifqidev.x_posetracker.utils.toBitmap

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfile2Binding? = null

    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfile2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity().application)
        profileViewModel =
            ViewModelProvider(requireActivity(), factory)[ProfileViewModel::class.java]

        profileViewModel.getUserProfile().observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.fullname.text = user.userName
                val profileImage = user.userProfile?.toBitmap()
                if (user.userProfile != null) {
                    binding.profileImage.setImageBitmap(profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.account_icon)
                }
            }
        }

        binding.editProfileButton.setOnClickListener {
            val intent = Intent(
                requireContext(),
                EditProfileActivity::class.java
            )

            startActivity(intent)
        }

        binding.tutorialButton.setOnClickListener {
            val intent = Intent(
                requireContext(),
                TutorialActivity::class.java
            )

            startActivity(intent)
        }

        binding.faqButton.setOnClickListener {
            val intent = Intent(
                requireContext(),
                QuestionActivity::class.java
            )

            startActivity(intent)
        }

        binding.donateButton.setOnClickListener {
            dialogDonate()
        }
    }

    private fun dialogDonate() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.donate_title))
            .setMessage(getString(R.string.donate_message))
            .setPositiveButton(getString(R.string.donate)) { _, _ ->

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://saweria.co/rifqimuafa")
                )

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_browser_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.later), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}