package com.rifqidev.x_posetracker.ui.faq

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.rifqidev.x_posetracker.R
import com.rifqidev.x_posetracker.adapter.FaqAdapter
import com.rifqidev.x_posetracker.data.FaqItem
import com.rifqidev.x_posetracker.databinding.ActivityQuestionBinding

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

        val questions = resources.getStringArray(R.array.faq_questions)
        val answers = resources.getStringArray(R.array.faq_answers)

        val faqList = questions.indices.map { index ->
            FaqItem(questions[index], answers[index])
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        val adapter = FaqAdapter(faqList)
        binding.recyclerViewFaq.adapter = adapter
        binding.recyclerViewFaq.layoutManager = LinearLayoutManager(this)

    }
}