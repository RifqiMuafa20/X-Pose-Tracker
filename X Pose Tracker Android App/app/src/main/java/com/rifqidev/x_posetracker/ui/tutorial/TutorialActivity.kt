package com.rifqidev.x_posetracker.ui.tutorial

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
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

        val youtubePlayerView = binding.youtubePlayerView

        lifecycle.addObserver(youtubePlayerView)

        binding.youtubePlayerView.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "dQw4w9WgXcQ"
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}