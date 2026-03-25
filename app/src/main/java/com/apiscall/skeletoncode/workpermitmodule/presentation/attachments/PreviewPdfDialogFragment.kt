package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.apiscall.skeletoncode.databinding.FragmentPreviewPdfBinding
import java.io.File

class PreviewPdfDialogFragment : DialogFragment() {

    private var _binding: FragmentPreviewPdfBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_FILE_PATH = "arg_file_path"

        fun newInstance(filePath: String): PreviewPdfDialogFragment {
            val args = Bundle().apply {
                putString(ARG_FILE_PATH, filePath)
            }
            return PreviewPdfDialogFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewPdfBinding.inflate(inflater, container, false)
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
                binding.pdfView.fromFile(file)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .load()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
