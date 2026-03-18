package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.databinding.FragmentPermitClosureBinding
import com.apiscall.skeletoncode.workpermitmodule.fileviewer.FileViewerActivity
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.ClosureAttachmentAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitClosureViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PermitClosureFragment : Fragment() {

    private var _binding: FragmentPermitClosureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PermitClosureViewModel by viewModels()
    private val args: PermitClosureFragmentArgs by navArgs()

    private lateinit var attachmentAdapter: ClosureAttachmentAdapter
    private var currentPhotoPath: String = ""

    // Activity result launchers
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val file = File(currentPhotoPath)
                if (file.exists()) {
                    viewModel.addAttachment(file, "image/jpeg")
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.addAttachment(it, "image/*")
            }
        }

    private val pickDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.addAttachment(it, "*/*")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermitClosureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        attachmentAdapter = ClosureAttachmentAdapter(
            onItemClick = { attachment ->
                // Open file viewer
                FileViewerActivity.openFile(
                    requireContext(),
                    attachment.filePath,
                    attachment.fileType,
                    attachment.fileName
                )
            },
            onDeleteClick = { attachment ->
                viewModel.removeAttachment(attachment.id)
            }
        )

        binding.rvAttachments.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = attachmentAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.closureResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnClose.isEnabled = false
                        binding.progressBar.visible()
                    }

                    is Resource.Success -> {
                        binding.btnClose.isEnabled = true
                        binding.progressBar.gone()
                        binding.root.showSnackbar("Permit closed successfully")
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.btnClose.isEnabled = true
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error closing permit")
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attachments.collectLatest { attachments ->
                attachmentAdapter.submitList(attachments)
                if (attachments.isEmpty()) {
                    binding.tvNoAttachments.visible()
                    binding.rvAttachments.gone()
                } else {
                    binding.tvNoAttachments.gone()
                    binding.rvAttachments.visible()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnAddPhoto.isEnabled = false
                        binding.btnAddDocument.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.btnAddPhoto.isEnabled = true
                        binding.btnAddDocument.isEnabled = true
                        binding.root.showSnackbar("File uploaded successfully")
                    }

                    is Resource.Error -> {
                        binding.btnAddPhoto.isEnabled = true
                        binding.btnAddDocument.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Upload failed")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddPhoto.setOnClickListener {
            showImagePickerOptions()
        }

        binding.btnAddDocument.setOnClickListener {
            pickDocumentLauncher.launch("*/*")
        }

        binding.btnClose.setOnClickListener {
            val remarks = binding.etRemarks.text?.toString()
            if (remarks.isNullOrBlank()) {
                binding.root.showSnackbar("Please enter closure remarks")
                return@setOnClickListener
            }
            viewModel.closePermit(args.permitId, remarks)
        }
    }

    private fun showImagePickerOptions() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile = createImageFile()
                currentPhotoPath = photoFile.absolutePath
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(photoURI)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}