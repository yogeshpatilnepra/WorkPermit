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
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryApprovalsFragment : Fragment() {

    private var _binding: FragmentApprovalsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApprovalQueueViewModel by viewModels()
    private lateinit var historyAdapter: PermitActionAdapter

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
        historyAdapter = PermitActionAdapter { permitId ->
            val action = ApprovalQueueFragmentDirections
                .actionApprovalQueueFragmentToPermitDetailsFragment(permitId)
            findNavController().navigate(action)
        }
        binding.rvPermits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentActions.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (historyAdapter.itemCount == 0) {
                            binding.progressBar.visible()
                            binding.emptyLayout.gone()
                        }
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        val actions = resource.data ?: emptyList()
                        
                        historyAdapter.submitList(actions) {
                            if (actions.isEmpty()) {
                                binding.rvPermits.gone()
                                binding.emptyLayout.visible()
                                binding.tvEmpty.text = "No approval history"
                                binding.emptyIcon.setImageResource(R.drawable.ic_empty)
                            } else {
                                binding.emptyLayout.gone()
                                binding.rvPermits.visible()
                            }
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        if (historyAdapter.itemCount == 0) {
                            binding.rvPermits.gone()
                            binding.emptyLayout.visible()
                            binding.tvEmpty.text = resource.message ?: "Error loading history"
                            binding.emptyIcon.setImageResource(R.drawable.ic_error)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}