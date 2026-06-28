package com.rifqidev.x_posetracker.utils.text_to_speech

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener

class SpeechManager(
    private val tts: TextToSpeech
) {

    private var speakingFeedback = false

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "feedback") {
                    speakingFeedback = false
                }
            }

            override fun onError(utteranceId: String?) {
                if (utteranceId == "feedback") {
                    speakingFeedback = false
                }

            }

        })
    }

    fun speak(text: String, type: SpeechType) {
        when(type){
            SpeechType.ACTIVITY -> {
                speakActivity(text)
            }

            SpeechType.REPETITION -> {
                speakRepetition(text)
            }

            SpeechType.FEEDBACK -> {
                speakFeedback(text)
            }
        }
    }

    private fun speakActivity(text: String){
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "activity"
        )

    }

    private fun speakRepetition(text: String){
        tts.stop()

        speakingFeedback = false

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "repetition"
        )
    }

    private fun speakFeedback(text: String){
        if (speakingFeedback) return

        speakingFeedback = true

        tts.speak(
            text,
            TextToSpeech.QUEUE_ADD,
            null,
            "feedback"
        )
    }
}