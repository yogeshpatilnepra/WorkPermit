package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apiscall.skeletoncode.databinding.FragmentCreatePermitBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.CreatePermitViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CreatePermitFragment : Fragment() {

    private var _binding: FragmentCreatePermitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePermitViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Spinner data
    private val permitTypes = listOf(
        "Hot Work", "Cold Work", "LOTO", "Confined Space",
        "Working at Height", "Lifting", "Live Equipment"
    )

    private val plants = listOf(
        "Sulzer Pump Plant 1", "Sulzer Pump Plant 2",
        "Sulzer Pump Plant 3", "Sulzer Pump Plant 4"
    )

    private val departments = listOf(
        "EHS", "Maintenance", "Production", "Projects", "Utilities"
    )

    private val areas = listOf(
        "Admin Block", "CNC Zone", "Foundry Support", "Maintenance Yard", "Tank Farm"
    )

    private val companies = listOf(
        "In-house", "ABC Contractors", "FastTrack Services",
        "Prime Industrial Works", "XYZ Engineering"
    )

    private val shifts = listOf(
        "Morning", "Evening", "Night", "General"
    )

    private var checklistItems = listOf<ChecklistItem>()
    private val checklistManager = ChecklistManager
    private var selectedPermitType: String = "Hot Work"
    private var selectedPlant: String = "Sulzer Pump Plant 1"
    private var selectedDepartment: String = "EHS"
    private var selectedArea: String = "Admin Block"
    private var selectedCompany: String = "In-house"
    private var selectedShift: String = "Morning"
    private var startDateTime: Date? = null
    private var endDateTime: Date? = null

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

        // Set default dates
        setDefaultDates()
    }

    private fun setupSpinners() {
        // Permit Type Spinner
        val typeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, permitTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPermitType.adapter = typeAdapter
        binding.spinnerPermitType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedPermitType = permitTypes[position]
                    updateChecklistForPermitType(selectedPermitType)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

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
                selectedPlant = plants[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Department Spinner
        val deptAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDepartment.adapter = deptAdapter
        binding.spinnerDepartment.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedDepartment = departments[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // Area Spinner
        val areaAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, areas)
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerArea.adapter = areaAdapter
        binding.spinnerArea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedArea = areas[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Company Spinner
        val companyAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companies)
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCompany.adapter = companyAdapter
        binding.spinnerCompany.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCompany = companies[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // Shift Spinner
        val shiftAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, shifts)
        shiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerShift.adapter = shiftAdapter
        binding.spinnerShift.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedShift = shifts[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setDefaultDates() {
        val calendar = Calendar.getInstance()
        startDateTime = calendar.time
        binding.tvStartDate.text =
            "${dateFormat.format(calendar.time)} ${timeFormat.format(calendar.time)}"

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        endDateTime = calendar.time
        binding.tvEndDate.text =
            "${dateFormat.format(calendar.time)} ${timeFormat.format(calendar.time)}"
    }

    private fun setupDatePickers() {
        binding.layoutStartDate.setOnClickListener {
            showDateTimePicker { dateTime ->
                startDateTime = dateTime
                binding.tvStartDate.text =
                    "${dateFormat.format(dateTime)} ${timeFormat.format(dateTime)}"
            }
        }

        binding.layoutEndDate.setOnClickListener {
            showDateTimePicker { dateTime ->
                endDateTime = dateTime
                binding.tvEndDate.text =
                    "${dateFormat.format(dateTime)} ${timeFormat.format(dateTime)}"
            }
        }
    }

    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Time")
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(0)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = selection
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateTimeSelected(calendar.time)
            }

            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnCreate.isEnabled = false
                        binding.btnSaveDraft.isEnabled = false
                        binding.progressBar.visible()
                    }

                    is Resource.Success -> {
                        binding.btnCreate.isEnabled = true
                        binding.btnSaveDraft.isEnabled = true
                        binding.progressBar.gone()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Success")
                            .setMessage("Permit created successfully with number: ${resource.data?.permitNumber}")
                            .setPositiveButton("OK") { _, _ ->
                                findNavController().popBackStack()
                            }
                            .show()
                    }

                    is Resource.Error -> {
                        binding.btnCreate.isEnabled = true
                        binding.btnSaveDraft.isEnabled = true
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
            createPermit(isDraft = true)
        }

        binding.btnCreate.setOnClickListener {
            createPermit(isDraft = false)
        }

        // Real-time validation
        binding.etTitle.addTextChangedListener { validateField(binding.etTitle, binding.tilTitle) }
        binding.etWorkerCount.addTextChangedListener {
            validateField(
                binding.etWorkerCount,
                binding.tilWorkerCount
            )
        }
        binding.etJobDescription.addTextChangedListener {
            validateField(
                binding.etJobDescription,
                binding.tilJobDescription
            )
        }
    }

    private fun validateField(
        editText: com.google.android.material.textfield.TextInputEditText,
        layout: com.google.android.material.textfield.TextInputLayout
    ): Boolean {
        return if (editText.text.isNullOrBlank()) {
            layout.error = "This field is required"
            false
        } else {
            layout.error = null
            true
        }
    }

    private fun createPermit(isDraft: Boolean) {
        // Validate required fields
        var isValid = true

        if (!validateField(binding.etTitle, binding.tilTitle)) isValid = false

        val workerCountStr = binding.etWorkerCount.text.toString()
        if (workerCountStr.isNullOrBlank()) {
            binding.tilWorkerCount.error = "Worker count is required"
            isValid = false
        } else {
            binding.tilWorkerCount.error = null
        }

        if (!validateField(binding.etJobDescription, binding.tilJobDescription)) isValid = false

        if (startDateTime == null) {
            binding.root.showSnackbar("Please select work start date/time")
            isValid = false
        }

        if (endDateTime == null) {
            binding.root.showSnackbar("Please select work end date/time")
            isValid = false
        }

        if (endDateTime != null && startDateTime != null && endDateTime!! <= startDateTime!!) {
            binding.root.showSnackbar("End date must be after start date")
            isValid = false
        }

        // Checklists validation - COMPULSORY
        if (!isDraft && !checklistManager.areRequiredItemsChecked(checklistItems)) {
            binding.root.showSnackbar("Please complete all safety checklist items")
            isValid = false
        }

        if (!isValid) return

        // Get current user
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                if (user == null) {
                    binding.root.showSnackbar("User not logged in")
                    return@collect
                }

                // Get checklist data
                val checklistData = checklistManager.getCheckedItems(checklistItems)

                // Create permit model
                val permit = PermitModel(
                    title = binding.etTitle.text.toString(),
                    permitType = selectedPermitType,
                    plant = selectedPlant,
                    department = selectedDepartment,
                    area = selectedArea,
                    company = selectedCompany,
                    shift = selectedShift,
                    workerCount = workerCountStr.toIntOrNull() ?: 0,
                    workStart = com.google.firebase.Timestamp(startDateTime!!),
                    workEnd = com.google.firebase.Timestamp(endDateTime!!),
                    riskAssessmentNo = binding.etRiskAssessmentNo.text.toString(),
                    jsaNo = binding.etJsaNo.text.toString(),
                    jobDescription = binding.etJobDescription.text.toString(),
                    status = if (isDraft) "draft" else "submitted",
                    approvalStage = if (isDraft) "draft" else "issuer_review",
                    requestorId = user.id,
                    requestorName = user.fullName,
                    requestorEmail = user.email,
                    // Checklist fields mapping
                    gasTesting = checklistData["gas_testing"] ?: false,
                    fireWatch = checklistData["fire_watch"] ?: false,
                    sparkShields = checklistData["spark_shields"] ?: false,
                    combustiblesRemoved = checklistData["combustibles_removed"] ?: false,
                    barricading = checklistData["barricading"] ?: false,
                    isolationPoints = checklistData["isolation_points"] ?: false,
                    locksApplied = checklistData["locks_applied"] ?: false,
                    locksVerified = checklistData["locks_verified"] ?: false,
                    zeroEnergyTest = checklistData["zero_energy_test"] ?: false,
                    hiddenSources = checklistData["hidden_sources"] ?: false,
                    oxygenLevel = checklistData["oxygen_level"] ?: false,
                    lelLevel = checklistData["lel_level"] ?: false,
                    toxicGases = checklistData["toxic_gases"] ?: false,
                    ventilation = checklistData["ventilation"] ?: false,
                    rescueEquipment = checklistData["rescue_equipment"] ?: false,
                    attendant = checklistData["attendant"] ?: false,
                    rescuePlan = checklistData["rescue_plan"] ?: false,
                    harnessInspection = checklistData["harness_inspection"] ?: false,
                    anchorPoints = checklistData["anchor_points"] ?: false,
                    fallProtection = checklistData["fall_protection"] ?: false,
                    scaffolding = checklistData["scaffolding"] ?: false,
                    rescuePlanHeight = checklistData["rescue_plan_height"] ?: false,
                    loadChart = checklistData["load_chart"] ?: false,
                    riggingInspection = checklistData["rigging_inspection"] ?: false,
                    qualifiedCrew = checklistData["qualified_crew"] ?: false,
                    dropZone = checklistData["drop_zone"] ?: false,
                    windSpeed = checklistData["wind_speed"] ?: false,
                    liftPlan = checklistData["lift_plan"] ?: false,
                    arcFlashAssessment = checklistData["arc_flash_assessment"] ?: false,
                    arcRatedPpe = checklistData["arc_rated_ppe"] ?: false,
                    liveWorkProcedure = checklistData["live_work_procedure"] ?: false,
                    voltageTesting = checklistData["voltage_testing"] ?: false,
                    boundaries = checklistData["boundaries"] ?: false,
                    basicIsolation = checklistData["basic_isolation"] ?: false,
                    correctPpe = checklistData["correct_ppe"] ?: false,
                    barricadingCold = checklistData["barricading_cold"] ?: false,
                    spillPrevention = checklistData["spill_prevention"] ?: false,
                    housekeeping = checklistData["housekeeping"] ?: false
                )

                viewModel.createPermit(permit)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateChecklistForPermitType(permitType: String) {
        checklistItems = checklistManager.getChecklistItems(permitType)
        checklistManager.inflateChecklistItems(
            binding.checklistContainer,
            checklistItems,
            layoutInflater
        )

        // Update subtitle based on permit type
        binding.tvChecklistSubtitle.text = when (permitType.lowercase()) {
            "hot work" -> "Hot Work Safety Requirements:"
            "loto" -> "Lockout/Tagout Safety Requirements:"
            "confined space" -> "Confined Space Entry Requirements:"
            "working at height" -> "Work at Height Safety Requirements:"
            "lifting" -> "Lifting Operation Safety Requirements:"
            "live equipment" -> "Live Equipment Work Requirements:"
            "cold work" -> "Cold Work Safety Requirements:"
            else -> "Please verify the following:"
        }
    }
}