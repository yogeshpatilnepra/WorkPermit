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
import com.apiscall.skeletoncode.databinding.FragmentApprovalQueueBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.PermitAdapter
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApprovalQueueFragment : Fragment() {

    private var _binding: FragmentApprovalQueueBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApprovalQueueViewModel by viewModels()
    private lateinit var permitAdapter: PermitAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovalQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadPendingApprovals()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter { permitId ->
            val action =
                ApprovalQueueFragmentDirections.actionApprovalQueueFragmentToPermitDetailsFragment(
                        permitId
                    )
            findNavController().navigate(action)
        }

        binding.rvApprovals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permitAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingApprovals.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.rvApprovals.gone()
                        binding.emptyLayout.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.emptyLayout.visible()
                            binding.tvEmpty.text = "No pending approvals"
                            binding.rvApprovals.gone()
                        } else {
                            binding.emptyLayout.gone()
                            binding.rvApprovals.visible()
                            permitAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.emptyLayout.visible()
                        binding.tvEmpty.text = resource.message ?: "Error loading approvals"
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actionResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show snackbar loading indicator only
                        binding.root.showSnackbar("Processing...")
                    }

                    is Resource.Success -> {
                        binding.root.showSnackbar("Permit approved successfully!")
                        // Reset action result to idle
                        viewModel.resetActionResult()
                        // Reload the list
                        viewModel.loadPendingApprovals()
                        // Navigate back to clear the current view
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Action failed")
                        viewModel.resetActionResult()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadPendingApprovals()
        }

        binding.fabRefresh.setOnClickListener {
            viewModel.loadPendingApprovals()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}