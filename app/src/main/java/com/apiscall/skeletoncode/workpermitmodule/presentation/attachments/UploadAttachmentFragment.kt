package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.apiscall.skeletoncode.databinding.FragmentUploadAttachmentBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UploadAttachmentFragment : Fragment() {

    private var _binding: FragmentUploadAttachmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttachmentViewModel by viewModels()
    private val args: UploadAttachmentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadAttachmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.btnUpload.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.btnUpload.isEnabled = true
                        binding.root.showSnackbar("Attachment uploaded successfully")
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.btnUpload.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Upload failed")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnChooseFile.setOnClickListener {
            showFileTypeDialog()
        }

        binding.btnUpload.setOnClickListener {
            val description = binding.etDescription.text?.toString()
            if (description.isNullOrBlank()) {
                binding.root.showSnackbar("Please add a description")
                return@setOnClickListener
            }
            viewModel.uploadAttachment(args.permitId, description)
        }

        binding.btnTakePhoto.setOnClickListener {
            simulatePhotoCapture()
        }
    }

    private fun showFileTypeDialog() {
        val options = arrayOf("PDF Document", "Image", "Other File")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select File Type")
            .setItems(options) { _, which ->
                val fileType = when (which) {
                    0 -> "PDF"
                    1 -> "Image"
                    else -> "Other"
                }
                binding.tvSelectedFile.text =
                    "Selected: sample_${fileType.lowercase()}.${fileType.lowercase()}"
                viewModel.setSelectedFile(fileType)
            }
            .show()
    }

    private fun simulatePhotoCapture() {
        binding.tvSelectedFile.text = "Selected: captured_photo.jpg"
        viewModel.setSelectedFile("Image")
        binding.root.showSnackbar("Photo captured (demo)")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}