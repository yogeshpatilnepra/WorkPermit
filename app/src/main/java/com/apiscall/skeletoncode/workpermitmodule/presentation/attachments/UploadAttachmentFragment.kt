package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments


import android.content.Intent
import android.net.Uri
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
import com.apiscall.skeletoncode.databinding.FragmentUploadAttachmentBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.AttachmentAdapter
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class UploadAttachmentFragment : Fragment() {

    private var _binding: FragmentUploadAttachmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttachmentViewModel by viewModels()
    private val args: UploadAttachmentFragmentArgs by navArgs()

    private lateinit var attachmentAdapter: AttachmentAdapter
    private var currentPhotoPath: String = ""
    private var selectedFileUri: Uri? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val file = File(currentPhotoPath)
                if (file.exists()) {
                    viewModel.uploadAttachment(args.permitId, file, "image/jpeg")
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.uploadAttachment(args.permitId, it, "image/*")
            }
        }

    private val pickDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.uploadAttachment(args.permitId, it, "*/*")
            }
        }

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

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadAttachments(args.permitId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        attachmentAdapter = AttachmentAdapter { attachment ->
            previewAttachment(attachment)
        }

        binding.rvAttachments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attachmentAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attachments.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        attachmentAdapter.submitList(resource.data)
                        if (resource.data.isNullOrEmpty()) {
                            binding.tvEmpty.visible()
                            binding.rvAttachments.gone()
                        } else {
                            binding.tvEmpty.gone()
                            binding.rvAttachments.visible()
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error loading attachments")
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.btnCamera.isEnabled = false
                        binding.btnGallery.isEnabled = false
                        binding.btnDocument.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.btnCamera.isEnabled = true
                        binding.btnGallery.isEnabled = true
                        binding.btnDocument.isEnabled = true
                        binding.root.showSnackbar("Attachment uploaded successfully!")
                        viewModel.loadAttachments(args.permitId)
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.btnCamera.isEnabled = true
                        binding.btnGallery.isEnabled = true
                        binding.btnDocument.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Upload failed")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnDocument.setOnClickListener {
            pickDocumentLauncher.launch("*/*")
        }

        binding.btnDone.setOnClickListener {
            findNavController().navigateUp()
        }
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

    private fun previewAttachment(attachment: Attachment) {
        when {
            attachment.fileType.contains("image") -> {
                // Preview image using Glide or similar
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(attachment.filePath)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "View Image"))
            }

            attachment.fileType.contains("pdf") -> {
                // Preview PDF
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(attachment.filePath)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "View PDF"))
            }

            else -> {
                // Open with default app
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(attachment.filePath)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Open File"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}