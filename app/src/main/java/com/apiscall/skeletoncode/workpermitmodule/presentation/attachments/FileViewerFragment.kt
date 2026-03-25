package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.apiscall.skeletoncode.databinding.FragmentFileViewerBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FileViewerFragment : Fragment() {

    private var _binding: FragmentFileViewerBinding? = null
    private val binding get() = _binding!!

    private val args: FileViewerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadFile()
    }

    private fun setupToolbar() {
        binding.toolbar.title = args.fileName
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadFile() {
        val path = args.filePath
        val type = args.fileType
        val file = File(path)

        if (!file.exists()) {
            showError("File not found at: $path")
            return
        }

        binding.progressBar.visible()

        when {
            type.contains("image") -> {
                binding.photoView.visible()
                Glide.with(this)
                    .load(file)
                    .into(binding.photoView)
                binding.progressBar.gone()
            }
            type.contains("pdf") -> {
                binding.pdfView.visible()
                try {
                    binding.pdfView.fromFile(file)
                        .onLoad { binding.progressBar.gone() }
                        .onError { showError("Error loading PDF") }
                        .load()
                } catch (e: Exception) {
                    showError("PDF View failed: ${e.message}")
                }
            }
            else -> {
                showError("Unsupported file type: $type")
            }
        }
    }

    private fun showError(message: String = "Failed to load file") {
        binding.progressBar.gone()
        binding.tvError.visible()
        binding.tvError.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}