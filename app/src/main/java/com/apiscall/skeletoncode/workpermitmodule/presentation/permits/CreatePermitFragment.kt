package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apiscall.skeletoncode.databinding.FragmentCreatePermitBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.CreatePermitViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreatePermitFragment : Fragment() {

    private var _binding: FragmentCreatePermitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePermitViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePermitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupDatePickers()
        setupObservers()
        setupListeners()
    }

    private fun setupSpinners() {
        // Permit Type Spinner
        val permitTypes = PermitType.values().map {
            it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() }
        }
        val typeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, permitTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPermitType.setAdapter(typeAdapter)

        binding.spinnerPermitType.setOnItemClickListener { _, _, position, _ ->
            viewModel.setPermitType(PermitType.values()[position])
        }

        // Location Spinner
        val locations = listOf(
            "Reactor Area - Unit A", "Tank Farm - Tank T-205",
            "Pipe Rack PR-200", "Compressor Station C-101", "Boiler House"
        )
        val locationAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.setAdapter(locationAdapter)

        binding.spinnerLocation.setOnItemClickListener { _, _, position, _ ->
            viewModel.setLocation(locations[position])
        }
    }

    private fun setupDatePickers() {
        binding.layoutStartDate.setOnClickListener {
            showDatePicker { timestamp ->
                viewModel.setStartDate(timestamp)
                binding.tvStartDate.text = dateFormat.format(Date(timestamp))
                showTimePicker { hour, minute ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = timestamp
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                    viewModel.setStartDate(calendar.timeInMillis)
                    binding.tvStartDate.text =
                        "${dateFormat.format(calendar.time)} ${timeFormat.format(calendar.time)}"
                }
            }
        }

        binding.layoutEndDate.setOnClickListener {
            showDatePicker { timestamp ->
                viewModel.setEndDate(timestamp)
                binding.tvEndDate.text = dateFormat.format(Date(timestamp))
                showTimePicker { hour, minute ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = timestamp
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                    viewModel.setEndDate(calendar.timeInMillis)
                    binding.tvEndDate.text =
                        "${dateFormat.format(calendar.time)} ${timeFormat.format(calendar.time)}"
                }
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

    private fun showTimePicker(onTimeSelected: (Int, Int) -> Unit) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Select Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            onTimeSelected(timePicker.hour, timePicker.minute)
        }

        timePicker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnNext.isEnabled = false
                        binding.progressBar.visible()
                    }

                    is Resource.Success -> {
                        binding.btnNext.isEnabled = true
                        binding.progressBar.gone()
                        resource.data?.let { permit ->
                            val action = CreatePermitFragmentDirections
                                .actionCreatePermitFragmentToDynamicFormFragment(permit.permitType)
                            findNavController().navigate(action)
                        }
                    }

                    is Resource.Error -> {
                        binding.btnNext.isEnabled = true
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error creating permit")
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

        binding.btnSaveDraft.setOnClickListener {
            savePermit(isDraft = true)
        }

        binding.btnNext.setOnClickListener {
            savePermit(isDraft = false)
        }
    }

    private fun savePermit(isDraft: Boolean) {
        val title = binding.etTitle.text?.toString()
        val description = binding.etDescription.text?.toString()

        if (title.isNullOrBlank()) {
            binding.root.showSnackbar("Please enter permit title")
            return
        }

        if (description.isNullOrBlank()) {
            binding.root.showSnackbar("Please enter description")
            return
        }

        viewModel.createPermit(
            title = title,
            description = description,
            isDraft = isDraft
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}