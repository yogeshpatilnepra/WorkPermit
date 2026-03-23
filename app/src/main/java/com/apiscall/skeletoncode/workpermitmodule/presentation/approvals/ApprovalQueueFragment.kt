package com.apiscall.skeletoncode.workpermitmodule.presentation.approvals


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.apiscall.skeletoncode.databinding.FragmentApprovalQueueBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApprovalQueueFragment : Fragment() {

    private var _binding: FragmentApprovalQueueBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApprovalQueueViewModel by viewModels()

    private lateinit var pagerAdapter: ApprovalsPagerAdapter
    private val fragments = listOf(
        PendingApprovalsFragment(),
        HistoryApprovalsFragment()
    )
    private val titles = listOf("Pending", "History")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovalQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = ApprovalsPagerAdapter(requireActivity(), fragments, titles)
        binding.viewPager.adapter = pagerAdapter

        // Attach TabLayout to ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        // Add page change callback to refresh data when tab changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> viewModel.loadPendingApprovals()
                    1 -> viewModel.loadRecentActions()
                }
            }
        })
    }

    private fun setupObservers() {
        // Observe approval result to show success message and refresh
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.approvalResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }

                    is Resource.Success -> {
                        binding.root.showSnackbar("Action completed successfully!")
                        viewModel.resetApprovalResult()
                        // Refresh current tab
                        val currentPosition = binding.viewPager.currentItem
                        if (currentPosition == 0) {
                            viewModel.loadPendingApprovals()
                        } else {
                            viewModel.loadRecentActions()
                        }
                        // Delay to allow refresh then navigate back
                        delay(500)
                        try {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        } catch (e: Exception) {
                        }
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Action failed")
                        viewModel.resetApprovalResult()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.fabRefresh.setOnClickListener {
            val currentPosition = binding.viewPager.currentItem
            if (currentPosition == 0) {
                viewModel.loadPendingApprovals()
            } else {
                viewModel.loadRecentActions()
            }
            binding.root.showSnackbar("Refreshing...")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}