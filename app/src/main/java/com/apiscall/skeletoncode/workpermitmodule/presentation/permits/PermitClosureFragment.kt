package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.apiscall.skeletoncode.databinding.FragmentPermitClosureBinding
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitClosureViewModel
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PermitClosureFragment : Fragment() {

    private var _binding: FragmentPermitClosureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PermitClosureViewModel by viewModels()
    private val args: PermitClosureFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermitClosureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.closureResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnClose.isEnabled = false
                        binding.progressBar.visible()
                    }

                    is Resource.Success -> {
                        binding.btnClose.isEnabled = true
                        binding.progressBar.gone()
                        binding.root.showSnackbar("Permit closed successfully")
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.btnClose.isEnabled = true
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error closing permit")
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

        binding.btnAddPhoto.setOnClickListener {
            // Simulate photo attachment
            viewModel.addMockAttachment()
            binding.root.showSnackbar("Photo attached (demo)")
        }

        binding.btnAddDocument.setOnClickListener {
            // Simulate document attachment
            viewModel.addMockAttachment()
            binding.root.showSnackbar("Document attached (demo)")
        }

        binding.btnClose.setOnClickListener {
            val remarks = binding.etRemarks.text?.toString()
            if (remarks.isNullOrBlank()) {
                binding.root.showSnackbar("Please enter closure remarks")
                return@setOnClickListener
            }
            viewModel.closePermit(args.permitId, remarks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}