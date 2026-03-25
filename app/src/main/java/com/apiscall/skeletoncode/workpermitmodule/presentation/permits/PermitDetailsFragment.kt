package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.formatDate
import com.apiscall.skeletoncode.workpermitmodule.utils.formatFull
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusColor
import com.apiscall.skeletoncode.workpermitmodule.utils.getStatusText
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.WorkerAdapter
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.loadImage
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var workerAdapter: WorkerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
            val action =
                PermitDetailsFragmentDirections.actionPermitDetailsFragmentToFileViewerFragment(
                    attachment.filePath,
                    attachment.fileType,
                    attachment.fileName
                )
            findNavController().navigate(action)
        }
        binding.rvAttachments.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = attachmentAdapter
        }

        workerAdapter = WorkerAdapter { logEntryId ->
            viewModel.workerSignOut(args.permitId, logEntryId)
        }
        binding.rvWorkerLog.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workerAdapter
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
        binding.tvPermitType.text =
            permit.permitType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
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

        // Progress Stepper
        setupStepper(permit)

        // Display checklist items
        displayChecklistItems(permit)

        // Setup action buttons based on permit stage and user role
        setupActionButtons(permit)

        // Attachments
        attachmentAdapter.submitList(permit.attachments)

        // Approval history
        approvalHistoryAdapter.submitList(permit.approvalHistory)
    }

    private fun setupStepper(permit: Permit) {
        binding.stepperContainer.removeAllViews()

        val stages = listOf(
            Triple("Created", permit.createdAt, true),
            Triple(
                "Issuer",
                permit.approvalHistory.find { it.user.role == Role.ISSUER }?.timestamp,
                permit.approvalHistory.any { it.user.role == Role.ISSUER }),
            Triple(
                "EHS",
                permit.approvalHistory.find { it.user.role == Role.EHS_OFFICER }?.timestamp,
                permit.approvalHistory.any { it.user.role == Role.EHS_OFFICER }),
            Triple(
                "Area Owner",
                permit.approvalHistory.find { it.user.role == Role.AREA_OWNER }?.timestamp,
                permit.approvalHistory.any { it.user.role == Role.AREA_OWNER }),
            Triple(
                "Sign In",
                permit.workerLog.firstOrNull()?.signInAt,
                permit.workerLog.isNotEmpty()),
            Triple(
                "Sign Out",
                permit.workerLog.find { it.signOutAt != null }?.signOutAt,
                permit.workerLog.isNotEmpty() && permit.workerLog.all { it.signOutAt != null }),
            Triple(
                "Closed",
                permit.closedAt,
                permit.status == com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.CLOSED
            )
        )

        stages.forEachIndexed { index, stage ->
            val itemView =
                layoutInflater.inflate(R.layout.item_stepper_step, binding.stepperContainer, false)
            val circle = itemView.findViewById<View>(R.id.stepCircle)
            val label = itemView.findViewById<TextView>(R.id.stepLabel)
            val time = itemView.findViewById<TextView>(R.id.stepTime)
            val line = itemView.findViewById<View>(R.id.stepLine)

            label.text = stage.first
            if (stage.third) {
                circle.setBackgroundResource(R.drawable.bg_status_approved)
                label.setTextColor(resources.getColor(R.color.safety_green, null))
                time.text = stage.second?.formatFull() ?: ""
                time.visible()
            } else {
                circle.setBackgroundResource(R.drawable.bg_status_draft)
                time.gone()
            }

            if (index == stages.size - 1) {
                line.gone()
            }

            binding.stepperContainer.addView(itemView)
        }
    }

    private fun displayChecklistItems(permit: Permit) {
        binding.checklistContainer.removeAllViews()

        val checklistItems = mutableListOf<Pair<String, Boolean>>()
        
        checklistItems.add("Toolbox Talk" to permit.toolboxTalkDone)
        checklistItems.add("PPE Verified" to permit.ppeVerified)

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
                checklistItems.add("Locks verified" to permit.locksVerified)
                checklistItems.add("Zero energy test performed" to permit.zeroEnergyTest)
                checklistItems.add("Hidden energy sources identified" to permit.hiddenSources)
            }

            "CONFINED_SPACE" -> {
                checklistItems.add("Oxygen level checked (19.5% - 23.5%)" to permit.oxygenLevel)
                checklistItems.add("Ventilation adequate" to permit.ventilation)
                checklistItems.add("Rescue equipment ready" to permit.rescueEquipment)
                checklistItems.add("Attendant assigned" to permit.attendant)
                checklistItems.add("Rescue plan reviewed" to permit.rescuePlan)
            }

            "WORK_AT_HEIGHT" -> {
                checklistItems.add("Harness inspected" to permit.harnessInspection)
                checklistItems.add("Anchor points certified" to permit.anchorPoints)
                checklistItems.add("Fall protection in place" to permit.fallProtection)
                checklistItems.add("Scaffolding inspected" to permit.scaffolding)
                checklistItems.add("Rescue plan for height reviewed" to permit.rescuePlanHeight)
            }

            "LIFTING" -> {
                checklistItems.add("Load chart verified" to permit.loadChart)
                checklistItems.add("Rigging inspected" to permit.riggingInspection)
                checklistItems.add("Qualified crew assigned" to permit.qualifiedCrew)
                checklistItems.add("Drop zone established" to permit.dropZone)
                checklistItems.add("Wind speed within limits" to permit.windSpeed)
                checklistItems.add("Lift plan approved" to permit.liftPlan)
            }

            "LIVE_EQUIPMENT" -> {
                checklistItems.add("Arc flash assessment done" to permit.arcFlashAssessment)
                checklistItems.add("Arc-rated PPE available" to permit.arcRatedPpe)
                checklistItems.add("Live work procedure followed" to permit.liveWorkProcedure)
                checklistItems.add("Voltage testing performed" to permit.voltageTesting)
                checklistItems.add("Boundaries established" to permit.boundaries)
            }

            "COLD_WORK" -> {
                checklistItems.add("Basic isolation done" to permit.basicIsolation)
                checklistItems.add("Correct PPE available" to permit.correctPpe)
                checklistItems.add("Barricading (Cold Work)" to permit.barricadingCold)
                checklistItems.add("Spill prevention in place" to permit.spillPrevention)
                checklistItems.add("Housekeeping maintained" to permit.housekeeping)
            }
        }

        val activeChecklists = checklistItems.filter { it.second }

        if (activeChecklists.isEmpty()) {
            binding.cardChecklist.visibility = View.GONE
        } else {
            binding.cardChecklist.visibility = View.VISIBLE
            activeChecklists.forEach { (text, isChecked) ->
                val checkBox =
                    com.google.android.material.checkbox.MaterialCheckBox(requireContext()).apply {
                        this.text = text
                        this.isChecked = isChecked
                        this.isEnabled = false
                        this.isClickable = false
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
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
            else -> stage.replace("_", " ").replaceFirstChar { it.uppercase() }
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
                binding.requestorActions.visibility = View.GONE
                
                val permitStatus = permit.status.name.lowercase()
                val role = user.role
                val stage = permit.approvalStage.lowercase().trim()

                // 1. Requestor Actions
                if (role == Role.REQUESTOR && permit.requester.id == user.id) {
                    if (permitStatus == "draft") {
                        binding.requestorActions.visibility = View.VISIBLE
                        binding.btnSubmit.visibility = View.VISIBLE
                        binding.btnDelete.visibility = View.VISIBLE
                        binding.btnSubmit.setOnClickListener { viewModel.submitDraftPermit(permit.id) }
                        binding.btnDelete.setOnClickListener { showDeleteConfirmDialog() }
                    } else if (permitStatus == "sent_back" || stage == "sent_back") {
                        binding.requestorActions.visibility = View.VISIBLE
                        binding.btnResubmit.visibility = View.VISIBLE
                        binding.btnResubmit.setOnClickListener { viewModel.resubmitPermit(permit.id) }
                    }
                }

                // 2. Approver Actions
                val isApproverTurn = when (stage) {
                    "issuer_review" -> role == Role.ISSUER || role == Role.ADMIN || role == Role.SUPERVISOR
                    "ehs_review" -> role == Role.EHS_OFFICER || role == Role.ADMIN || role == Role.SUPERVISOR
                    "area_owner_review" -> role == Role.AREA_OWNER || role == Role.ADMIN || role == Role.SUPERVISOR
                    else -> false
                }
                if (isApproverTurn && permitStatus != "closed" && permitStatus != "rejected") {
                    binding.approvalActions.visibility = View.VISIBLE
                    val approverRole = if (role == Role.ADMIN || role == Role.SUPERVISOR) stage.substringBefore("_review") else role.name.lowercase()
                    setupApprovalButtons(approverRole)
                }

                // 3. Worker Actions (Sign In / Multi-Worker List)
                if (role == Role.WORKER || role == Role.ADMIN || role == Role.SUPERVISOR) {
                    if (stage == "issued" || stage == "active") {
                        binding.workerActions.visibility = View.VISIBLE
                        binding.btnSignInWorker.setOnClickListener {
                            showWorkerSignInDialog(permit.id)
                        }
                    }
                }

                // 4. Supervisor Closure
                if ((role == Role.SUPERVISOR || role == Role.ADMIN) && (stage == "issued" || stage == "active")) {
                    binding.closureActions.visibility = View.VISIBLE
                    binding.btnClosePermitClosure.setOnClickListener {
                        showClosureDialog()
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
        val textInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilComment)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        textInputLayout.hint = "Comments (Optional)"

        MaterialAlertDialogBuilder(requireContext()).setTitle("$title Permit").setView(dialogView)
            .setPositiveButton(title) { _, _ ->
                val comments = editText.text?.toString()
                if (isApprove) {
                    viewModel.approvePermit(args.permitId, role, comments)
                    findNavController().popBackStack()
                } else {
                    viewModel.rejectPermit(args.permitId, role, comments ?: "Rejected")
                    findNavController().popBackStack()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showSendBackDialog(role: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val textInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilComment)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        textInputLayout.hint = "Reason for Send Back"

        MaterialAlertDialogBuilder(requireContext()).setTitle("Send Back Permit")
            .setMessage("Provide reason for sending back").setView(dialogView)
            .setPositiveButton("Send Back") { _, _ ->
                val comments = editText.text?.toString() ?: "Sent back for revision"
                viewModel.sendBackPermit(args.permitId, role, comments)
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showClosureDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val textInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilComment)
        val editText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)

        textInputLayout.hint = "Closure Remarks"

        MaterialAlertDialogBuilder(requireContext()).setTitle("Close Permit")
            .setMessage("Provide closure remarks").setView(dialogView)
            .setPositiveButton("Close") { _, _ ->
                val comments = editText.text?.toString() ?: "Work completed"
                viewModel.closePermit(args.permitId, comments)
                findNavController().popBackStack()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Permit")
            .setMessage("Are you sure you want to permanently delete this draft permit?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePermit(args.permitId)
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWorkerSignInDialog(permitId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val textInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilComment)
        val editText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComment)
        
        textInputLayout.hint = "Worker Name"
        textInputLayout.error = null
        editText.setText("")

        val dialog = MaterialAlertDialogBuilder(requireContext()).setTitle("Worker Sign In")
            .setMessage("Please enter the name of the worker signing in.").setView(dialogView)
            .setPositiveButton("Sign In", null) // Set to null to override later for validation
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val workerName = editText.text?.toString()?.trim()
            if (workerName.isNullOrBlank()) {
                textInputLayout.error = "Name is required"
            } else {
                viewModel.workerSignIn(permitId, workerName)
                dialog.dismiss()
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddAttachment.setOnClickListener {
            val action =
                PermitDetailsFragmentDirections.actionPermitDetailsFragmentToUploadAttachmentFragment(
                    args.permitId
                )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}