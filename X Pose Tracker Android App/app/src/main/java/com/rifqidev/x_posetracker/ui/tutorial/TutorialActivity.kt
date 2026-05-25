package com.rifqidev.x_posetracker.ui.tutorial

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.transition.TransitionManager
import com.rifqidev.x_posetracker.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

        binding.youtubeIcon.setOnClickListener {
            val videoId = "H6-kATEYaKU"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
            intent.putExtra("force_fullscreen", true)
            intent.putExtra("finish_on_ended", true)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                )
                startActivity(webIntent)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}