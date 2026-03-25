package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.apiscall.skeletoncode.databinding.FragmentPreviewImageBinding
import com.bumptech.glide.Glide
import java.io.File

class PreviewImageDialogFragment : DialogFragment() {

    private var _binding: FragmentPreviewImageBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_FILE_PATH = "arg_file_path"

        fun newInstance(filePath: String): PreviewImageDialogFragment {
            val args = Bundle().apply {
                putString(ARG_FILE_PATH, filePath)
            }
            return PreviewImageDialogFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filePath = arguments?.getString(ARG_FILE_PATH)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        filePath?.let {
            val file = File(it)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .into(binding.photoView)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
