package com.apiscall.skeletoncode.workpermitmodule.presentation.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentHomeBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters.QuickAction
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters.QuickActionAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters.RecentPermitsAdapter
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters.StatCard
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.adapters.StatCardAdapter
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var statAdapter: StatCardAdapter
    private lateinit var quickActionAdapter: QuickActionAdapter
    private lateinit var recentPermitsAdapter: RecentPermitsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerViews() {
        // Stats RecyclerView
        statAdapter = StatCardAdapter()
        binding.rvStats.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = statAdapter
        }

        // Quick Actions RecyclerView
        quickActionAdapter = QuickActionAdapter { action ->
            when (action) {
                QuickAction.NEW_PERMIT -> {
                    findNavController().navigate(R.id.action_homeFragment_to_createPermitFragment)
                }
                QuickAction.SEARCH -> {
                    findNavController().navigate(R.id.action_homeFragment_to_searchFilterFragment)
                }
                QuickAction.SYNC -> {
                    viewModel.refreshDashboard()
                }
            }
        }
        binding.rvQuickActions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = quickActionAdapter
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                user?.let {
                    val canCreatePermit = when (it.role) {
                        Role.REQUESTOR, Role.SUPERVISOR, Role.ADMIN -> true
                        else -> false
                    }
                    quickActionAdapter.updateNewPermitVisibility(canCreatePermit)
                }
            }
        }
        // Recent Permits RecyclerView
        recentPermitsAdapter = RecentPermitsAdapter { permitId ->
            val action = HomeFragmentDirections.actionHomeFragmentToPermitDetailsFragment(permitId)
            findNavController().navigate(action)
        }
        binding.rvRecentPermits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentPermitsAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                user?.let {
                    binding.tvUserName.text = it.fullName
                    binding.tvUserRole.text = it.role.name.replace("_", " ").lowercase()
                        .replaceFirstChar { char -> char.uppercase() }

                    // Hide New Permit button based on role
                    val canCreatePermit = when (it.role) {
                        Role.REQUESTOR, Role.SUPERVISOR, Role.ADMIN -> true
                        else -> false
                    }

                    // Update Quick Actions visibility - only show New Permit if allowed
                    quickActionAdapter.updateNewPermitVisibility(canCreatePermit)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardStats.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                    }
                    is Resource.Success -> {
                        binding.progressBar.gone()
                        resource.data?.let { stats ->
                            statAdapter.submitList(
                                listOf(
                                    StatCard(
                                        "Total",
                                        stats.totalPermits.toString(),
                                        R.drawable.ic_permits
                                    ),
                                    StatCard("Pending", stats.pendingApprovals.toString(), R.drawable.ic_pending),
                                    StatCard("Active", stats.activePermits.toString(), R.drawable.ic_active),
                                    StatCard("Expiring", stats.expiringToday.toString(), R.drawable.ic_warning)
                                )
                            )
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.gone()
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentPermits.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        recentPermitsAdapter.submitList(resource.data)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myPermitsFragment)
        }

        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}