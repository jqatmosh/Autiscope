package com.autiscope.ui.matchGame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.autiscope.databinding.FragmentMatchingGameBinding
import com.autiscope.model.Question
import com.autiscope.ui.questions.BaseQuestionFragment

class MatchingGameFragment : BaseQuestionFragment() {
    private var _binding: FragmentMatchingGameBinding? = null
    private val binding get() = _binding!!


    companion object {
        const val TAG: String = "MatchingGameFragment"
        private const val ARG_QUESTION: String = "args_question"

        fun newInstance(question: Question): MatchingGameFragment {
            return MatchingGameFragment().apply {
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
        _binding = FragmentMatchingGameBinding.inflate(inflater, container, false)
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
                binding.image1.setImageResource(it.images[0])
                if (it.images.size >= 2) {
                    binding.image2.setImageResource(it.images[1])
                }
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