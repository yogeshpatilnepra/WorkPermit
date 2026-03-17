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
import com.apiscall.skeletoncode.workpermitmodule.utils.Constants
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

        setupObservers()
        setupListeners()

        // Start QR scan simulation
        simulateQRScan()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scanResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.tvStatus.text = "Scanning..."
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        resource.data?.let { permitId ->
                            binding.tvStatus.text = "QR Code Scanned Successfully!"
                            binding.tvPermitId.text = "Permit ID: $permitId"
                            binding.btnViewPermit.visible()
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.tvStatus.text = "Scan Failed"
                        binding.root.showSnackbar(resource.message ?: "Failed to scan QR code")
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

        binding.btnScan.setOnClickListener {
            simulateQRScan()
        }

        binding.btnViewPermit.setOnClickListener {
            viewModel.navigateToPermit()
        }

        binding.btnSimulate.setOnClickListener {
            simulateQRScan()
        }
    }

    private fun simulateQRScan() {
        binding.progressBar.visible()
        binding.btnViewPermit.gone()
        binding.tvStatus.text = "Simulating QR Scan..."

        // Simulate QR scan after delay
        binding.root.postDelayed({
            viewModel.processScanResult("${Constants.QR_CODE_PREFIX}permit_1")
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}