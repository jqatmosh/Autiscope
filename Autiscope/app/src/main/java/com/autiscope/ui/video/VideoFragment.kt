package com.autiscope.ui.video

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.autiscope.databinding.FragmentVideoBinding
import com.autiscope.model.Question
import com.autiscope.ui.questions.BaseQuestionFragment


class VideoFragment : BaseQuestionFragment() {
    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG: String = "VideoFragment"
        private const val ARG_QUESTION: String = "args_question"

        fun newInstance(question: Question): VideoFragment {
            return VideoFragment().apply {
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
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        val question: Question? = arguments?.getParcelable(ARG_QUESTION)
        question?.audio?.let { audio ->
            playAudio(audio)
        }
        val videoUrl = question?.video
        val videoUri =
            Uri.parse("android.resource://${requireActivity().packageName}/${videoUrl}")
        binding.videoContainer.setVideoURI(videoUri)
        val mediaController = MediaController(requireActivity())
        mediaController.setAnchorView(binding.videoContainer)
        binding.videoContainer.setMediaController(mediaController)
        binding.videoContainer.start()

    }
}