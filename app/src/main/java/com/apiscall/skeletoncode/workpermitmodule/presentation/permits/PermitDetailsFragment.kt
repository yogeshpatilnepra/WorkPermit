package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentPermitDetailsBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.ApprovalHistoryAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.AttachmentAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitDetailViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.*
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

        setupToolbar()
        setupRecyclerViews()
        setupObservers()
        setupListeners()

        viewModel.loadPermitDetails(args.permitId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerViews() {
        approvalHistoryAdapter = ApprovalHistoryAdapter()
        binding.rvApprovalHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = approvalHistoryAdapter
        }

        attachmentAdapter = AttachmentAdapter { attachment ->
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
                    is Resource.Loading -> {}
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
        // Basic Information
        binding.tvPermitNumber.text = permit.permitNumber
        binding.tvPermitType.text = permit.permitType.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
        binding.tvTitle.text = permit.title
        binding.tvDescription.text = permit.description
        binding.tvLocation.text = permit.location
        binding.tvPlant.text = permit.plant ?: "N/A"
        binding.tvDepartment.text = permit.department ?: "N/A"
        binding.tvArea.text = permit.area ?: "N/A"
        binding.tvCompany.text = permit.company ?: "N/A"
        binding.tvShift.text = permit.shift ?: "N/A"
        binding.tvWorkerCount.text = permit.workerCount.toString()

        // Dates
        binding.tvStartDate.text = permit.startDate.formatDate()
        binding.tvEndDate.text = permit.endDate.formatDate()

        // Status
        binding.tvStatus.text = getStatusText(permit.status.name)
        binding.tvStatus.setBackgroundResource(getStatusColor(permit.status.name))
        binding.tvApprovalStage.text = getApprovalStageText(permit.approvalStage)

        // Requester Info
        binding.tvRequesterName.text = permit.requester.fullName
        binding.tvRequesterDept.text = permit.requester.department
        binding.tvRequesterEmail.text = permit.requester.email
        binding.ivRequester.loadImage(permit.requester.profileImageUrl)

        // Documents
        binding.tvRiskAssessmentNo.text = permit.riskAssessmentNo ?: "Not provided"
        binding.tvJsaNo.text = permit.jsaNo ?: "Not provided"

        // Safety Verification - Show only if there's data
        var hasSafetyVerification = false

        if (permit.toolboxTalkDone) {
            binding.cbToolboxTalkDone.visibility = View.VISIBLE
            binding.cbToolboxTalkDone.isChecked = true
            hasSafetyVerification = true
        } else {
            binding.cbToolboxTalkDone.visibility = View.GONE
        }

        if (permit.ppeVerified) {
            binding.cbPpeVerified.visibility = View.VISIBLE
            binding.cbPpeVerified.isChecked = true
            hasSafetyVerification = true
        } else {
            binding.cbPpeVerified.visibility = View.GONE
        }

        binding.cardSafetyVerification.visibility =
            if (hasSafetyVerification) View.VISIBLE else View.GONE

        // Display checklist items
        displayChecklistItems(permit)

        // Setup action buttons based on permit stage and user role
        setupActionButtons(permit)

        // Attachments
        attachmentAdapter.submitList(permit.attachments)

        // Approval history
        approvalHistoryAdapter.submitList(permit.approvalHistory)
    }

    private fun displayChecklistItems(permit: Permit) {
        binding.checklistContainer.removeAllViews()

        val checklistItems = mutableListOf<Pair<String, Boolean>>()

        when (permit.permitType.name) {
            "HOT_WORK" -> {
                checklistItems.add("Gas testing (LEL < 10%)" to permit.gasTesting)
                checklistItems.add("Fire watch present" to permit.fireWatch)
                checklistItems.add("Spark shields / spark arrestors used" to permit.sparkShields)
                checklistItems.add("Combustible materials removed" to permit.combustiblesRemoved)
                checklistItems.add("Barricading and warning signs" to permit.barricading)
            }

            "LOTO" -> {
                checklistItems.add("Isolation points identified" to permit.isolationPoints)
                checklistItems.add("Locks applied" to permit.locksApplied)
                checklistItems.add("Zero energy test performed" to permit.zeroEnergyTest)
            }

            "CONFINED_SPACE" -> {
                checklistItems.add("Oxygen level checked" to permit.oxygenLevel)
                checklistItems.add("Ventilation adequate" to permit.ventilation)
                checklistItems.add("Rescue equipment ready" to permit.rescueEquipment)
                checklistItems.add("Attendant assigned" to permit.attendant)
            }

            "WORK_AT_HEIGHT" -> {
                checklistItems.add("Harness inspected" to permit.harnessInspection)
                checklistItems.add("Anchor points certified" to permit.anchorPoints)
                checklistItems.add("Fall protection in place" to permit.fallProtection)
            }

            "LIFTING" -> {
                checklistItems.add("Load chart verified" to permit.loadChart)
                checklistItems.add("Rigging inspected" to permit.riggingInspection)
                checklistItems.add("Qualified crew assigned" to permit.qualifiedCrew)
                checklistItems.add("Drop zone established" to permit.dropZone)
            }

            "LIVE_EQUIPMENT" -> {
                checklistItems.add("Arc flash assessment done" to permit.arcFlashAssessment)
                checklistItems.add("Arc-rated PPE available" to permit.arcRatedPpe)
                checklistItems.add("Voltage testing performed" to permit.voltageTesting)
            }

            "COLD_WORK" -> {
                checklistItems.add("Basic isolation done" to permit.basicIsolation)
                checklistItems.add("Correct PPE available" to permit.correctPpe)
                checklistItems.add("Housekeeping maintained" to permit.housekeeping)
            }
        }

        if (checklistItems.isEmpty()) {
            binding.cardChecklist.visibility = View.GONE
        } else {
            binding.cardChecklist.visibility = View.VISIBLE
            checklistItems.forEach { (text, isChecked) ->
                val checkBox =
                    com.google.android.material.checkbox.MaterialCheckBox(requireContext()).apply {
                        this.text = text
                        this.isChecked = isChecked
                        this.isEnabled = false
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 8 }
                    }
                binding.checklistContainer.addView(checkBox)
            }
        }
    }

    private fun getApprovalStageText(stage: String): String {
        return when (stage) {
            "issuer_review" -> "Pending Issuer Review"
            "ehs_review" -> "Pending EHS Review"
            "area_owner_review" -> "Pending Area Owner Review"
            "issued" -> "Approved - Ready for Work"
            "active" -> "Work in Progress"
            "closed" -> "Closed"
            "rejected" -> "Rejected"
            "sent_back" -> "Sent Back for Revision"
            else -> stage.replace("_", " ").capitalize()
        }
    }

    private fun setupActionButtons(permit: Permit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                if (user == null) return@collect

                // Hide all action layouts first
                binding.approvalActions.visibility = View.GONE
                binding.closureActions.visibility = View.GONE
                binding.workerActions.visibility = View.GONE

                // Show appropriate actions based on permit stage and user role
                when (permit.approvalStage) {
                    "issuer_review" -> {
                        if (user.role == Role.ISSUER) {
                            binding.approvalActions.visibility = View.VISIBLE
                            setupApprovalButtons("issuer")
                        }
                    }

                    "ehs_review" -> {
                        if (user.role == Role.EHS_OFFICER) {
                            binding.approvalActions.visibility = View.VISIBLE
                            setupApprovalButtons("ehs")
                        }
                    }

                    "area_owner_review" -> {
                        if (user.role == Role.AREA_OWNER) {
                            binding.approvalActions.visibility = View.VISIBLE
                            setupApprovalButtons("area_owner")
                        }
                    }

                    "issued" -> {
                        if (user.role == Role.SUPERVISOR) {
                            binding.closureActions.visibility = View.VISIBLE
                            binding.btnClosePermitClosure.setOnClickListener {
                                showClosureDialog()
                            }
                        }
                        if (user.role == Role.WORKER) {
                            binding.workerActions.visibility = View.VISIBLE
                            binding.btnSignInWorker.visibility = View.VISIBLE
                            binding.btnSignOutWorker.visibility = View.GONE
                            binding.btnSignInWorker.setOnClickListener {
                                showWorkerSignInDialog(permit.id)
                            }
                        }
                    }

                    "active" -> {
                        if (user.role == Role.SUPERVISOR) {
                            binding.closureActions.visibility = View.VISIBLE
                            binding.btnClosePermitClosure.setOnClickListener {
                                showClosureDialog()
                            }
                        }
                        if (user.role == Role.WORKER) {
                            binding.workerActions.visibility = View.VISIBLE
                            binding.btnSignInWorker.visibility = View.GONE
                            binding.btnSignOutWorker.visibility = View.VISIBLE
                            binding.btnSignOutWorker.setOnClickListener {
                                showWorkerSignOutDialog(permit.id)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupApprovalButtons(role: String) {
        binding.btnApprove.setOnClickListener {
            showApprovalDialog("Approve", isApprove = true, role = role)
        }
        binding.btnReject.setOnClickListener {
            showApprovalDialog("Reject", isApprove = false, role = role)
        }
        binding.btnSendBack.setOnClickListener {
            showSendBackDialog(role)
        }
    }

    private fun showApprovalDialog(title: String, isApprove: Boolean, role: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("$title Permit")
            .setView(dialogView)
            .setPositiveButton(title) { _, _ ->
                val comments = editText.text?.toString()
                if (isApprove) {
                    viewModel.approvePermit(args.permitId, role, comments)
                } else {
                    viewModel.rejectPermit(args.permitId, role, comments ?: "Rejected")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSendBackDialog(role: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Send Back Permit")
            .setMessage("Provide reason for sending back")
            .setView(dialogView)
            .setPositiveButton("Send Back") { _, _ ->
                val comments = editText.text?.toString() ?: "Sent back for revision"
                viewModel.sendBackPermit(args.permitId, role, comments)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClosureDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Close Permit")
            .setMessage("Provide closure remarks")
            .setView(dialogView)
            .setPositiveButton("Close") { _, _ ->
                val comments = editText.text?.toString() ?: "Work completed"
                viewModel.closePermit(args.permitId, comments)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWorkerSignInDialog(permitId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Worker Sign In")
            .setMessage("Enter worker name")
            .setView(dialogView)
            .setPositiveButton("Sign In") { _, _ ->
                val workerName = editText.text?.toString()?.takeIf { it.isNotBlank() } ?: "Worker"
                viewModel.workerSignIn(permitId, workerName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWorkerSignOutDialog(permitId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Worker Sign Out")
            .setMessage("Confirm worker sign out")
            .setView(dialogView)
            .setPositiveButton("Sign Out") { _, _ ->
                val workerName = editText.text?.toString()?.takeIf { it.isNotBlank() } ?: "Worker"
                viewModel.workerSignOut(permitId, workerName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAttachmentPreview(filePath: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attachment Preview")
            .setMessage("Preview not implemented in demo")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupListeners() {
        binding.btnAddAttachment.setOnClickListener {
            val action = PermitDetailsFragmentDirections
                .actionPermitDetailsFragmentToUploadAttachmentFragment(args.permitId)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}