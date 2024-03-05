package com.autiscope.ui.questions

import android.media.MediaPlayer

import androidx.fragment.app.Fragment

open class BaseQuestionFragment : Fragment() {
    private var mediaPlayer: MediaPlayer? = null
    fun playAudio(audio: Int) {
        mediaPlayer = MediaPlayer.create(requireActivity(), audio)

        // Start playing the audio file
        mediaPlayer?.start()

        // You can also add listeners for playback completion, errors, etc.
        mediaPlayer?.setOnCompletionListener {
            // Handle playback completion
        }

        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            // Handle errors
            false // Return false to indicate that the error has not been handled
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}