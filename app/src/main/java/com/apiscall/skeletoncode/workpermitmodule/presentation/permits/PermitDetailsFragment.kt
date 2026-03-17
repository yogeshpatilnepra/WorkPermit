package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentPermitDetailsBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.ApprovalHistoryAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.AttachmentAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitDetailViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.formatDate
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusColor
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusText
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.loadImage
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PermitDetailsFragment : Fragment() {

    private var _binding: FragmentPermitDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PermitDetailViewModel by viewModels()
    private val args: PermitDetailsFragmentArgs by navArgs()

    private lateinit var approvalHistoryAdapter: ApprovalHistoryAdapter
    private lateinit var attachmentAdapter: AttachmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermitDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupObservers()
        setupListeners()

        viewModel.loadPermitDetails(args.permitId)
    }

    private fun setupRecyclerViews() {
        approvalHistoryAdapter = ApprovalHistoryAdapter()
        binding.rvApprovalHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = approvalHistoryAdapter
        }

        attachmentAdapter = AttachmentAdapter { attachment ->
            // Handle attachment click - preview
            showAttachmentPreview(attachment.filePath)
        }
        binding.rvAttachments.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = attachmentAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.permitDetails.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.contentLayout.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.contentLayout.visible()
                        resource.data?.let { permit ->
                            bindPermitData(permit)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(
                            resource.message ?: "Error loading permit details"
                        )
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading on action button
                    }

                    is Resource.Success -> {
                        binding.root.showSnackbar("Action completed successfully")
                        viewModel.loadPermitDetails(args.permitId)
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Action failed")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun bindPermitData(permit: Permit) {
        binding.tvPermitNumber.text = permit.permitNumber
        binding.tvPermitType.text = permit.permitType.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
        binding.tvTitle.text = permit.title
        binding.tvDescription.text = permit.description
        binding.tvLocation.text = permit.location
        binding.tvStatus.text = permit.status.getStatusText()
        binding.tvStatus.setBackgroundResource(permit.status.getStatusColor())
        binding.tvStartDate.text = permit.startDate.formatDate()
        binding.tvEndDate.text = permit.endDate.formatDate()
        binding.tvRequesterName.text = permit.requester.fullName
        binding.tvRequesterDept.text = permit.requester.department
        binding.ivRequester.loadImage(permit.requester.profileImageUrl)

        // Show/hide action buttons based on status and role
        setupActionButtons(permit)

        // Submit attachments
        attachmentAdapter.submitList(permit.attachments)

        // Submit approval history
        approvalHistoryAdapter.submitList(permit.approvalHistory)

        // Show workers if any
        if (permit.workers.isNotEmpty()) {
            binding.workersSection.visible()
            binding.tvWorkersCount.text = "${permit.workers.size} workers signed in"
            // You can add a horizontal recycler view for workers here
        } else {
            binding.workersSection.gone()
        }
    }

    private fun setupActionButtons(permit: Permit) {
        when (permit.status) {
            PermitStatus.PENDING_ISSUER_APPROVAL,
            PermitStatus.PENDING_AREA_OWNER_APPROVAL,
            PermitStatus.PENDING_EHS_APPROVAL -> {
                binding.approvalActions.visible()
                binding.btnApprove.setOnClickListener {
                    showApprovalDialog("Approve", isApprove = true)
                }
                binding.btnReject.setOnClickListener {
                    showApprovalDialog("Reject", isApprove = false)
                }
                binding.btnSendBack.setOnClickListener {
                    showSendBackDialog()
                }
            }

            PermitStatus.APPROVED -> {
                binding.activeActions.visible()
                binding.btnStartWork.setOnClickListener {
                    // Navigate to worker sign in
                    val action = PermitDetailsFragmentDirections
                        .actionPermitDetailsFragmentToWorkerSignInFragment(permit.id)
                    findNavController().navigate(action)
                }
            }

            PermitStatus.ACTIVE -> {
                binding.activeActions.visible()
                binding.btnStartWork.text = "Sign Out Worker"
                binding.btnStartWork.setOnClickListener {
                    val action = PermitDetailsFragmentDirections
                        .actionPermitDetailsFragmentToWorkerSignOutFragment(permit.id)
                    findNavController().navigate(action)
                }
                binding.btnClosePermit.visible()
                binding.btnClosePermit.setOnClickListener {
                    val action = PermitDetailsFragmentDirections
                        .actionPermitDetailsFragmentToPermitClosureFragment(permit.id)
                    findNavController().navigate(action)
                }
            }

            else -> {
                binding.approvalActions.gone()
                binding.activeActions.gone()
            }
        }

        binding.btnAddAttachment.setOnClickListener {
            val action = PermitDetailsFragmentDirections
                .actionPermitDetailsFragmentToUploadAttachmentFragment(permit.id)
            findNavController().navigate(action)
        }
    }

    private fun showApprovalDialog(title: String, isApprove: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("$title Permit")
            .setView(dialogView)
            .setPositiveButton(title) { _, _ ->
                val comments = editText.text?.toString()
                if (isApprove) {
                    viewModel.approvePermit(args.permitId, comments)
                } else {
                    viewModel.rejectPermit(args.permitId, comments ?: "Rejected")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSendBackDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Send Back Permit")
            .setMessage("Provide reason for sending back")
            .setView(dialogView)
            .setPositiveButton("Send Back") { _, _ ->
                val comments = editText.text?.toString() ?: "Sent back for revision"
                viewModel.sendBackPermit(args.permitId, comments)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAttachmentPreview(filePath: String) {
        // Implement attachment preview (image/pdf viewer)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attachment Preview")
            .setMessage("Preview not implemented in demo")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}