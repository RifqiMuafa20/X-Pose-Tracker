package com.rifqidev.x_posetracker.ui.splashscreen

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val splashDelay = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES
        )

        supportActionBar?.hide()

        lifecycleScope.launch {

            delay(splashDelay)

            if (
                !isFinishing &&
                !isDestroyed &&
                !supportFragmentManager.isStateSaved
            ) {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        LoadingSplashScreenFragment()
                    )
                    .commit()
            }
        }
    }
}