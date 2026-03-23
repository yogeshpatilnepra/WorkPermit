package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.databinding.FragmentActivePermitsBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.PermitAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitListViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ActivePermitsFragment : Fragment() {

    private var _binding: FragmentActivePermitsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PermitListViewModel by viewModels()

    private lateinit var permitAdapter: PermitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivePermitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadActivePermits()
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter(
            onItemClick = { permitId ->
                val action = ActivePermitsFragmentDirections.actionActivePermitsFragmentToPermitDetailsFragment(permitId)
                findNavController().navigate(action)
            }
        )

        binding.rvPermits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permitAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.permits.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.rvPermits.gone()
                        binding.tvEmpty.gone()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.rvPermits.gone()
                            binding.tvEmpty.visible()
                            binding.tvEmpty.text = "No active permits"
                        } else {
                            binding.rvPermits.visible()
                            binding.tvEmpty.gone()
                            permitAdapter.submitList(resource.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.rvPermits.gone()
                        binding.tvEmpty.visible()
                        binding.root.showSnackbar(resource.message ?: "Error loading permits")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadActivePermits()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}