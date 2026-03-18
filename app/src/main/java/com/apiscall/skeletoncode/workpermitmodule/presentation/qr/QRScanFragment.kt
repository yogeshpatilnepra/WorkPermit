package com.apiscall.skeletoncode.workpermitmodule.presentation.qr


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apiscall.skeletoncode.databinding.FragmentQRScanBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QRScanFragment : Fragment() {

    private var _binding: FragmentQRScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QRViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQRScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.btnSimulateScan.setOnClickListener {
            viewModel.simulateScan()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scanResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.btnSimulateScan.isEnabled = false
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.btnSimulateScan.isEnabled = true
                        val permitId = resource.data ?: return@collectLatest
                        val action =
                            QRScanFragmentDirections.actionQRScanFragmentToPermitDetailsFragment(
                                permitId
                            )
                        findNavController().navigate(action)
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.btnSimulateScan.isEnabled = true
                        binding.root.showSnackbar(resource.message ?: "Scan failed")
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}