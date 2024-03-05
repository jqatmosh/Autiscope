package com.autiscope.ui.similarites

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.autiscope.R
import com.autiscope.databinding.FragmentSimilaritiesBinding
import com.autiscope.model.Question
import com.autiscope.ui.questions.BaseQuestionFragment

class FindSimilaritiesImagesFragment : BaseQuestionFragment() {
    private var _binding: FragmentSimilaritiesBinding? = null
    private val binding get() = _binding!!
    private var question: Question? = null

    companion object {
        const val TAG: String = "FindSimilaritiesImagesFragment"
        private const val ARG_QUESTION: String = "args_question"

        fun newInstance(question: Question): FindSimilaritiesImagesFragment {
            return FindSimilaritiesImagesFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(ARG_QUESTION, question)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSimilaritiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        question = arguments?.getParcelable(ARG_QUESTION)
        question?.let { question ->
            if (!question.images.isNullOrEmpty() && question.images.size >= 4) {
                binding.apply {
                    image1.setImageResource(question.images[0])
                    image2.setImageResource(question.images[1])
                    image3.setImageResource(question.images[2])
                    image4.setImageResource(question.images[3])
                }
                startTimer(15)
            }
            if (!question.audios.isNullOrEmpty()) {
                playAudio(question.audios[0])
            }
        }
    }

    private fun startTimer(timeLeftInSeconds: Int) {
        object : CountDownTimer(timeLeftInSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                onTimeEnded()
                cancel()
            }
        }.also { it.start() }
    }

    private fun onTimeEnded() {

        binding.apply {
            image1.setImageResource(R.drawable.ic_white_square)
            image2.setImageResource(R.drawable.ic_white_square)
            image3.setImageResource(R.drawable.ic_white_square)
            image4.setImageResource(R.drawable.ic_white_square)
            setClickListeners()
        }
        if (!question?.audios.isNullOrEmpty() && question?.audios?.size!! >= 2) {
            playAudio(question?.audios!![1])
        }
    }

    private fun setClickListeners() {
        question?.let { question ->
            if (!question.images.isNullOrEmpty() && question.images.size >= 4) {
                binding.apply {
                    image1.setOnClickListener {
                        image1.setImageResource(question.images[0])
                    }
                    image2.setOnClickListener {
                        image2.setImageResource(question.images[1])
                    }
                    image3.setOnClickListener {
                        image3.setImageResource(question.images[2])
                    }
                    image4.setOnClickListener {
                        image4.setImageResource(question.images[3])
                    }
                }
            }
        }
    }
}