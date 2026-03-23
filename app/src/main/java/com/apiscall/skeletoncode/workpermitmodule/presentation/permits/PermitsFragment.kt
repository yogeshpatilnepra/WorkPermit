package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentPermitsBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.PermitAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitsViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PermitsFragment : Fragment() {

    private var _binding: FragmentPermitsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PermitsViewModel by viewModels()
    private lateinit var permitAdapter: PermitAdapter

    private val plants = listOf(
        "All Plants",
        "Sulzer Pump Plant 1",
        "Sulzer Pump Plant 2",
        "Sulzer Pump Plant 3",
        "Sulzer Pump Plant 4"
    )

    private val statuses = listOf(
        "all", "draft", "issuer review", "ehs review", "area owner review",
        "issued", "in progress", "closed", "rejected", "sent back", "expired"
    )

    private val permitTypes = listOf(
        "All Types", "Hot Work", "LOTO", "Confined Space", "Working at Height",
        "Lifting", "Live Equipment", "Cold Work"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSpinners()
        setupSearch()
        setupObservers()
        setupListeners()

        viewModel.loadCurrentUser()
        viewModel.loadPermits()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter(
            onItemClick = { permitId ->
                val action =
                    PermitsFragmentDirections.actionPermitsFragmentToPermitDetailsFragment(permitId)
                findNavController().navigate(action)
            }
        )

        binding.rvPermits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permitAdapter
        }
    }


    private fun setupSpinners() {
        // Plant Spinner
        val plantAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plants)
        plantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPlant.adapter = plantAdapter
        binding.spinnerPlant.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.updatePlantFilter(plants[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Status Spinner
        val statusAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.updateStatusFilter(statuses[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Permit Type Spinner
        val typeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, permitTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.updateTypeFilter(permitTypes[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                user?.let {
                    // Show/hide create button based on role
                    val canCreatePermit = when (it.role) {
                        Role.ADMIN, Role.SUPERVISOR, Role.REQUESTOR -> true
                        else -> false
                    }
                    binding.fabAdd.isVisible = canCreatePermit
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.permits.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.rvPermits.gone()
                        binding.emptyLayout.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.emptyLayout.visible()
                            binding.rvPermits.gone()
                        } else {
                            binding.emptyLayout.gone()
                            binding.rvPermits.visible()
                            permitAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.emptyLayout.visible()
                        binding.tvEmpty.text = resource.message ?: "Error loading permits"
                        binding.root.showSnackbar(resource.message ?: "Error loading permits")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_permitsFragment_to_createPermitFragment)
        }

        binding.btnClearFilters.setOnClickListener {
            binding.spinnerPlant.setSelection(0)
            binding.spinnerStatus.setSelection(0)
            binding.spinnerType.setSelection(0)
            binding.etSearch.text?.clear()
            viewModel.clearFilters()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadPermits()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}