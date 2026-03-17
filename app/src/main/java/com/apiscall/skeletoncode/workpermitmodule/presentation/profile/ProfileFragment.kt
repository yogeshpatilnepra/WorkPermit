package com.apiscall.skeletoncode.workpermitmodule.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentProfileBinding
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.apiscall.skeletoncode.workpermitmodule.utils.gone
import com.apiscall.skeletoncode.workpermitmodule.utils.loadImage
import com.apiscall.skeletoncode.workpermitmodule.utils.showSnackbar
import com.apiscall.skeletoncode.workpermitmodule.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

        viewModel.loadUserProfile()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visible()
                        binding.contentLayout.gone()
                    }

                    is Resource.Success -> {
                        binding.progressBar.gone()
                        binding.contentLayout.visible()
                        resource.data?.let { user ->
                            bindUserData(user)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.gone()
                        binding.root.showSnackbar(resource.message ?: "Error loading profile")
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logoutResult.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }

                    is Resource.Success -> {
                        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                    }

                    is Resource.Error -> {
                        binding.root.showSnackbar(resource.message ?: "Logout failed")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun bindUserData(user: User) {
        binding.ivProfile.loadImage(user.profileImageUrl)
        binding.tvName.text = user.fullName
        binding.tvRole.text = user.role.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
        binding.tvEmail.text = user.email
        binding.tvPhone.text = user.phoneNumber ?: "Not provided"
        binding.tvDepartment.text = user.department
        binding.tvEmployeeId.text = user.employeeId
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditProfile.setOnClickListener {
            binding.root.showSnackbar("Edit profile (demo)")
        }

        binding.btnChangePassword.setOnClickListener {
            binding.root.showSnackbar("Change password (demo)")
        }

        binding.btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationsFragment)
        }

        binding.btnSettings.setOnClickListener {
            binding.root.showSnackbar("Settings (demo)")
        }

        binding.btnHelp.setOnClickListener {
            binding.root.showSnackbar("Help (demo)")
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About")
            .setMessage("Digital Permit to Work System\nVersion 1.0.0\n\nA comprehensive solution for managing industrial work permits.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}