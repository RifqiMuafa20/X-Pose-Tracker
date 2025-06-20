package com.rifqidev.x_posetracker.ui.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.FragmentLoadingSplashScreenBinding
import com.rifqidev.x_posetracker.ui.welcome.WelcomeFragment

class LoadingSplashScreenFragment : Fragment() {

    private var _binding: FragmentLoadingSplashScreenBinding? = null
    private val delay: Long = 1000

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingSplashScreenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
        }, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}