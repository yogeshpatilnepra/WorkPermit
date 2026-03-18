package com.apiscall.skeletoncode.workpermitmodule.presentation.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.FragmentLoginBinding
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

        // Set default values in ViewModel
        viewModel.onUsernameChanged(binding.etUsername.text.toString())
        viewModel.onPasswordChanged(binding.etPassword.text.toString())
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            // Update ViewModel with current text before login
            viewModel.onUsernameChanged(binding.etUsername.text.toString())
            viewModel.onPasswordChanged(binding.etPassword.text.toString())
            viewModel.login()
        }

        binding.tvForgotPassword.setOnClickListener {
            showDemoCredentialsDialog()
        }

        // Update ViewModel when text changes
        binding.etUsername.setOnEditorActionListener { _, _, _ ->
            viewModel.onUsernameChanged(binding.etUsername.text.toString())
            false
        }

        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            viewModel.onPasswordChanged(binding.etPassword.text.toString())
            false
        }

        // Add text change listeners
        binding.etUsername.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.onUsernameChanged(s.toString())
            }
        })

        binding.etPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.onPasswordChanged(s.toString())
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnLogin.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.btnLogin.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }

                    is Resource.Error -> {
                        binding.btnLogin.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(
                            binding.root,
                            resource.message ?: "Login failed",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is Resource.Idle -> {
                        binding.btnLogin.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showDemoCredentialsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Demo Credentials")
            .setMessage(
                """
                Use any of these credentials:
                
                contractor1 / password123
                issuer1 / password123
                ehs1 / password123
                areaowner1 / password123
                supervisor1 / password123
                worker1 / password123
                """.trimIndent()
            )
            .setPositiveButton("Got it", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}