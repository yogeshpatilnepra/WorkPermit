package com.apiscall.skeletoncode.workpermitmodule.presentation.notifications


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.databinding.FragmentNotificationsBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            viewModel.markAsRead(notification.id)
            if (notification.permitId != null) {
                // Navigate to permit details
                // findNavController().navigate(...)
            }
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.rvNotifications.gone()
                        binding.emptyState.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        if (resource.data.isNullOrEmpty()) {
                            binding.rvNotifications.gone()
                            binding.emptyState.visible()
                        } else {
                            binding.rvNotifications.visible()
                            binding.emptyState.gone()
                            notificationAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error loading notifications")
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markAllResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.root.showSnackbar("All notifications marked as read")
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Error marking notifications")
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

        binding.btnMarkAllRead.setOnClickListener {
            viewModel.markAllAsRead()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadNotifications()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}