package com.apiscall.skeletoncode.workpermitmodule.presentation.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.apiscall.skeletoncode.databinding.FragmentWorkerSignInBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkerSignInFragment : Fragment() {

    private var _binding: FragmentWorkerSignInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkerViewModel by viewModels()
    private val args: WorkerSignInFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

        viewModel.loadAvailableWorkers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.workers.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.workerList.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.workerList.visible()
                        // Setup worker list adapter here
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error loading workers")
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signInResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnSignIn.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.btnSignIn.isEnabled = true
                        binding.root.showSnackbar("Worker signed in successfully")
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.btnSignIn.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Sign in failed")
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

        binding.btnSignIn.setOnClickListener {
            val workerId = "worker1" // In real app, get selected worker
            viewModel.signInWorker(args.permitId, workerId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}