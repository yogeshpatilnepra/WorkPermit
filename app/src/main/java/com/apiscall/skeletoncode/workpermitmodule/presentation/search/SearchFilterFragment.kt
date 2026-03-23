package com.apiscall.skeletoncode.workpermitmodule.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.databinding.FragmentSearchFilterBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.adapters.PermitAdapter
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SearchFilterFragment : Fragment() {

    private var _binding: FragmentSearchFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var permitAdapter: PermitAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSpinners()
        setupDatePickers()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        permitAdapter = PermitAdapter(onItemClick = {

        })

        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permitAdapter
        }
    }

    private fun setupSpinners() {
        // Status Spinner
        val statuses = listOf("All") + PermitStatus.values().map {
            it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() }
        }
        val statusAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Permit Type Spinner
        val types = listOf("All") + PermitType.values().map {
            it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() }
        }
        val typeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter
    }

    private fun setupDatePickers() {
        binding.layoutStartDate.setOnClickListener {
            showDatePicker { timestamp ->
                viewModel.setDateFrom(timestamp)
                binding.tvStartDate.text = dateFormat.format(Date(timestamp))
            }
        }

        binding.layoutEndDate.setOnClickListener {
            showDatePicker { timestamp ->
                viewModel.setDateTo(timestamp)
                binding.tvEndDate.text = dateFormat.format(Date(timestamp))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.rvResults.gone()
                        binding.tvNoResults.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.rvResults.gone()
                            binding.tvNoResults.visible()
                        } else {
                            binding.rvResults.visible()
                            binding.tvNoResults.gone()
                            permitAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Search failed")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.btnClearFilters.setOnClickListener {
            clearFilters()
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text?.toString()
        if (!query.isNullOrBlank()) {
            viewModel.search(query)
        } else {
            applyFilters()
        }
    }

    private fun applyFilters() {
        val status = if (binding.spinnerStatus.selectedItemPosition > 0) {
            PermitStatus.values()[binding.spinnerStatus.selectedItemPosition - 1]
        } else null

        val type = if (binding.spinnerType.selectedItemPosition > 0) {
            PermitType.values()[binding.spinnerType.selectedItemPosition - 1]
        } else null

        viewModel.filterPermits(status, type)
    }

    private fun clearFilters() {
        binding.etSearch.text?.clear()
        binding.spinnerStatus.setSelection(0)
        binding.spinnerType.setSelection(0)
        binding.tvStartDate.text = "Select date"
        binding.tvEndDate.text = "Select date"
        viewModel.clearFilters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}