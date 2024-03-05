package com.autiscope.ui.imageQuestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.autiscope.databinding.FragmentImageQuestionBinding
import com.autiscope.model.Question
import com.autiscope.ui.questions.BaseQuestionFragment

class ImageQuestionFragment : BaseQuestionFragment() {
    private var _binding: FragmentImageQuestionBinding? = null
    private val binding get() = _binding!!


    companion object {
        const val TAG: String = "ImageQuestionFragment"
        private const val ARG_QUESTION: String = "args_question"

        fun newInstance(question: Question): ImageQuestionFragment {
            return ImageQuestionFragment().apply {
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
        _binding = FragmentImageQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

    }

    private fun initialize() {
        val question: Question? = arguments?.getParcelable(ARG_QUESTION)
        question?.let {
            if (!it.images.isNullOrEmpty()) {
                binding.image.setImageResource(it.images[0])
            }
            it.audio?.let { audio ->
                playAudio(audio)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}