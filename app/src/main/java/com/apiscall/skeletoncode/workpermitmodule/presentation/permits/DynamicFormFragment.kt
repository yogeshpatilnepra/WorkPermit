package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentDynamicFormBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FieldType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FormField
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.DynamicFormViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DynamicFormFragment : Fragment() {

    private var _binding: FragmentDynamicFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DynamicFormViewModel by viewModels()
    private val args: DynamicFormFragmentArgs by navArgs()

    private val fieldViews = mutableMapOf<String, View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDynamicFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        setupListeners()

        viewModel.loadFormFields(args.permitType)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.formFields.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.scrollView.gone()
                        binding.errorLayout.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.errorLayout.visible()
                            binding.tvError.text = "No form fields available for this permit type."
                            binding.scrollView.gone()
                        } else {
                            binding.errorLayout.gone()
                            buildForm(resource.data)
                            binding.scrollView.visible()
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.errorLayout.visible()
                        binding.tvError.text = resource.message ?: "Failed to load form"
                        binding.scrollView.gone()
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.submitResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnSubmit.isEnabled = false
                        binding.btnSaveDraft.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.btnSubmit.isEnabled = true
                        binding.btnSaveDraft.isEnabled = true
                        binding.root.showSnackbar("Permit submitted successfully")
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }

                    is Resource.Error -> {
                        binding.btnSubmit.isEnabled = true
                        binding.btnSaveDraft.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Submission failed")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun buildForm(fields: List<FormField>) {
        binding.formContainer.removeAllViews()
        fieldViews.clear()

        fields.sortedBy { it.order }.forEach { field ->
            val view = createFieldView(field)
            binding.formContainer.addView(view)
            fieldViews[field.id] = view

            // Add bottom margin
            val params = view.layoutParams as? ViewGroup.MarginLayoutParams
            params?.setMargins(0, 0, 0, 16)
            view.layoutParams = params
        }
    }

    private fun createFieldView(field: FormField): View = when (field.type) {
        FieldType.TEXT, FieldType.NUMBER -> createTextField(field)
        FieldType.TEXTAREA -> createTextAreaField(field)
        FieldType.DROPDOWN -> createDropdownField(field)
        FieldType.CHECKBOX -> createCheckboxField(field)
        FieldType.RADIO -> createRadioGroupField(field)
        FieldType.DATE, FieldType.TIME -> createDateTimeField(field)
        else -> createTextField(field)
    }

    private fun createTextField(field: FormField): View {
        val layout = TextInputLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = if (field.isRequired) "${field.label} *" else field.label
            isHintEnabled = true
            if (field.type == FieldType.NUMBER) {
                endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            }
        }

        val editText = TextInputEditText(requireContext()).apply {
            inputType = when (field.type) {
                FieldType.NUMBER -> android.text.InputType.TYPE_CLASS_NUMBER
                else -> android.text.InputType.TYPE_CLASS_TEXT
            }
            hint = field.placeholder
            field.defaultValue?.let { setText(it) }

            doAfterTextChanged {
                viewModel.updateFieldValue(field.id, it?.toString() ?: "")
            }
        }

        layout.addView(editText)
        return layout
    }

    private fun createTextAreaField(field: FormField): View {
        val layout = TextInputLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = if (field.isRequired) "${field.label} *" else field.label
            isHintEnabled = true
        }

        val editText = TextInputEditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            hint = field.placeholder
            minLines = 3
            maxLines = 5
            field.defaultValue?.let { setText(it) }

            doAfterTextChanged {
                viewModel.updateFieldValue(field.id, it?.toString() ?: "")
            }
        }

        layout.addView(editText)
        return layout
    }

    private fun createDropdownField(field: FormField): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val textView = TextView(requireContext()).apply {
            text = if (field.isRequired) "${field.label} *" else field.label
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_primary, null))
        }

        val spinner = Spinner(requireContext()).apply {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                field.options
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.updateFieldValue(field.id, field.options[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        layout.addView(textView)
        layout.addView(spinner)
        return layout
    }

    private fun createCheckboxField(field: FormField): View {
        val checkBox = MaterialCheckBox(requireContext()).apply {
            text = field.label
            isChecked = field.defaultValue?.toBoolean() ?: false

            setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateFieldValue(field.id, isChecked.toString())
            }
        }
        return checkBox
    }

    private fun createRadioGroupField(field: FormField): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val textView = TextView(requireContext()).apply {
            text = if (field.isRequired) "${field.label} *" else field.label
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_primary, null))
        }
        layout.addView(textView)

        val radioGroup = RadioGroup(requireContext()).apply {
            orientation = RadioGroup.VERTICAL
        }

        field.options.forEachIndexed { index, option ->
            val radioButton = MaterialRadioButton(requireContext()).apply {
                text = option
                id = index
            }
            radioGroup.addView(radioButton)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedOption = field.options[checkedId]
            viewModel.updateFieldValue(field.id, selectedOption)
        }

        layout.addView(radioGroup)
        return layout
    }

    private fun createDateTimeField(field: FormField): View {
        val layout = TextInputLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = if (field.isRequired) "${field.label} *" else field.label
            isHintEnabled = true
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(android.R.drawable.ic_menu_today)
        }

        val editText = TextInputEditText(requireContext()).apply {
            isFocusable = false
            isClickable = true
            hint = field.placeholder

            setOnClickListener {
                showDateTimePicker(field.type) { dateTime ->
                    setText(dateTime)
                    viewModel.updateFieldValue(field.id, dateTime)
                }
            }
        }

        layout.addView(editText)
        return layout
    }

    private fun showDateTimePicker(type: FieldType, onSelected: (String) -> Unit) {
        if (type == FieldType.DATE) {
            val datePicker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(selection))
                onSelected(date)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        } else {
            val timePicker = com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                .setTitleText("Select Time")
                .build()
            timePicker.addOnPositiveButtonClickListener {
                val time = String.format(
                    Locale.getDefault(), "%02d:%02d",
                    timePicker.hour, timePicker.minute
                )
                onSelected(time)
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }
    }

    private fun setupListeners() {
        binding.btnSaveDraft.setOnClickListener {
            if (viewModel.validateForm()) {
                viewModel.saveDraft()
                binding.root.showSnackbar("Draft saved")
            } else {
                binding.root.showSnackbar("Please fill all required fields")
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (viewModel.validateForm()) {
                viewModel.submitForm()
            } else {
                binding.root.showSnackbar("Please fill all required fields")
            }
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadFormFields(args.permitType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}