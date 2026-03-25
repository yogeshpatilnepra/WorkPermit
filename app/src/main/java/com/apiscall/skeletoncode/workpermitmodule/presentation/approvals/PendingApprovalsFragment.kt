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

    // Using viewModels({ requireParentFragment() }) to share state with parent ApprovalQueueFragment
    // and correctly clear state when the user logs out and navigates away
    private val viewModel: ApprovalQueueViewModel by viewModels({ requireParentFragment() })
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
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter(
            currentUserRole = viewModel.currentUser.value?.role,
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
                        if (permitAdapter.itemCount == 0) {
                            binding.progressBar.visible()
                            binding.emptyLayout.gone()
                        }
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        val permits = resource.data ?: emptyList()
                        
                        permitAdapter.submitList(permits) {
                            if (permits.isEmpty()) {
                                binding.rvPermits.gone()
                                binding.emptyLayout.visible()
                                binding.tvEmpty.text = "No pending approvals"
                            } else {
                                binding.emptyLayout.gone()
                                binding.rvPermits.visible()
                            }
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        if (permitAdapter.itemCount == 0) {
                            binding.rvPermits.gone()
                            binding.emptyLayout.visible()
                            binding.tvEmpty.text = resource.message ?: "Error loading approvals"
                        } else {
                            binding.root.showSnackbar(resource.message ?: "Update failed")
                        }
                    }

                    else -> {}
                }
            }
        }

        // Subscribing to user role to pass to adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                user?.let {
                    permitAdapter.setCurrentUserRole(it.role)
                }
            }
        }

        // Action results observers (Approve/Reject/Send Back)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.approvalResult.collectLatest { resource ->
                if (resource is Resource.Success) {
                    binding.root.showSnackbar("Permit approved successfully!")
                    viewModel.resetApprovalResult()
                } else if (resource is Resource.Error) {
                    binding.root.showSnackbar(resource.message ?: "Approval failed")
                    viewModel.resetApprovalResult()
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
                    action == "Send Back" -> viewModel.sendBackPermit(permitId, comments ?: "Sent back")
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