package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentMyPermitsBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.PermitAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.MyPermitsViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPermitsFragment : Fragment() {

    private var _binding: FragmentMyPermitsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPermitsViewModel by viewModels()

    private lateinit var permitAdapter: PermitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPermitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadMyPermits()
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter { permitId ->
            val action =
                MyPermitsFragmentDirections.actionMyPermitsFragmentToPermitDetailsFragment(permitId)
            findNavController().navigate(action)
        }

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
                        binding.emptyState.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.rvPermits.gone()
                            binding.emptyState.visible()
                            binding.tvEmpty.text = "No permits found"
                        } else {
                            binding.rvPermits.visible()
                            binding.emptyState.gone()
                            permitAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.rvPermits.gone()
                        binding.emptyState.visible()
                        binding.tvEmpty.text = resource.message ?: "Error loading permits"
                        binding.root.showSnackbar(resource.message ?: "Error loading permits")
                    }

                    else -> {}
                }
            }
        }

        // Observe user role to show/hide FAB
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                user?.let {
                    // Show FAB only for requestor, supervisor, admin
                    val canCreatePermit = when (it.role) {
                        com.apiscall.skeletoncode.workpermitmodule.domain.models.Role.REQUESTOR,
                        com.apiscall.skeletoncode.workpermitmodule.domain.models.Role.SUPERVISOR,
                        com.apiscall.skeletoncode.workpermitmodule.domain.models.Role.ADMIN -> true

                        else -> false
                    }
                    binding.fabAdd.isVisible = canCreatePermit
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_myPermitsFragment_to_createPermitFragment)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadMyPermits()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}