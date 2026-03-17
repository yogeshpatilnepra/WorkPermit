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
import com.apiscall.skeletoncode.databinding.FragmentWorkerSignOutBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkerSignOutFragment : Fragment() {

    private var _binding: FragmentWorkerSignOutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkerViewModel by viewModels()
    private val args: WorkerSignOutFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerSignOutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

        viewModel.loadSignedInWorkers(args.permitId)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signOutResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnSignOut.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.btnSignOut.isEnabled = true
                        binding.root.showSnackbar("Worker signed out successfully")
                        findNavController().popBackStack()
                    }

                    is Resource.Error -> {
                        binding.btnSignOut.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Sign out failed")
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

        binding.btnSignOut.setOnClickListener {
            val workerId = "worker1" // In real app, get selected worker
            viewModel.signOutWorker(args.permitId, workerId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}