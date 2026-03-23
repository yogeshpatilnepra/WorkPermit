package com.apiscall.skeletoncode.workpermitmodule.presentation.approvals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentApprovalsListBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.PermitAdapter
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PendingApprovalsFragment : Fragment() {

    private var _binding: FragmentApprovalsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApprovalQueueViewModel by viewModels()
    private lateinit var permitAdapter: PermitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovalsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        viewModel.loadPendingApprovals()
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter(
            onItemClick = { permitId ->
                val action = ApprovalQueueFragmentDirections
                    .actionApprovalQueueFragmentToPermitDetailsFragment(permitId)
                findNavController().navigate(action)
            },
            onApproveClick = { permitId ->
                showApprovalDialog(permitId, "Approve", isApprove = true)
            },
            onRejectClick = { permitId ->
                showApprovalDialog(permitId, "Reject", isApprove = false)
            },
            onSendBackClick = { permitId ->
                showApprovalDialog(permitId, "Send Back", isApprove = false)
            }
        )
        binding.rvPermits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permitAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingApprovals.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Only show progress bar if list is empty
                        if (permitAdapter.currentList.isEmpty()) {
                            binding.progressBar.visible()
                            binding.rvPermits.gone()
                        }
                        binding.emptyLayout.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.rvPermits.gone()
                            binding.emptyLayout.visibility = View.VISIBLE
                            binding.tvEmpty.text = "No pending approvals"
                            binding.emptyIcon.setImageResource(R.drawable.ic_empty)
                        } else {
                            binding.emptyLayout.gone()
                            binding.rvPermits.visible()
                            permitAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.rvPermits.gone()
                        binding.emptyLayout.visibility = View.VISIBLE
                        binding.tvEmpty.text = resource.message ?: "Error loading approvals"
                        binding.emptyIcon.setImageResource(R.drawable.ic_error)
                    }

                    else -> {
                        binding.progressBar.gone()
                    }
                }
            }
        }

        // Observe approval result
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.approvalResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Optional: Show a global loading overlay or disable buttons
                    }

                    is Resource.Success -> {
                        binding.root.showSnackbar("Permit approved successfully!")
                        viewModel.resetApprovalResult()
                        // No need to call loadPendingApprovals() manually
                        // Firestore Flow handles the list update automatically
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Approval failed")
                        viewModel.resetApprovalResult()
                    }

                    else -> {}
                }
            }
        }

        // Observe rejection result
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rejectionResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        binding.root.showSnackbar("Permit rejected!")
                        viewModel.resetRejectionResult()
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Rejection failed")
                        viewModel.resetRejectionResult()
                    }

                    else -> {}
                }
            }
        }

        // Observe send back result
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sendBackResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        binding.root.showSnackbar("Permit sent back for revision!")
                        viewModel.resetSendBackResult()
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Action failed")
                        viewModel.resetSendBackResult()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun showApprovalDialog(permitId: String, action: String, isApprove: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_approval_comment, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.etComment)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("$action Permit")
            .setView(dialogView)
            .setPositiveButton(action) { _, _ ->
                val comments = editText.text?.toString()
                when {
                    isApprove -> viewModel.approvePermit(permitId, comments)
                    action == "Reject" -> viewModel.rejectPermit(permitId, comments ?: "Rejected")
                    action == "Send Back" -> viewModel.sendBackPermit(
                        permitId,
                        comments ?: "Sent back for revision"
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
