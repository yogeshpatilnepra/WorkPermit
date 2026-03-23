package com.apiscall.skeletoncode.workpermitmodule.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)

        // Set up bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)

        // Hide/show bottom navigation based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.permitDetailsFragment,
                R.id.createPermitFragment,
                R.id.dynamicFormFragment,
                R.id.uploadAttachmentFragment,
                R.id.qrScanFragment,
                R.id.workerSignInFragment,
                R.id.workerSignOutFragment,
                R.id.permitClosureFragment,
                R.id.searchFilterFragment -> {
                    bottomNav.isVisible = false
                }

                else -> {
                    bottomNav.isVisible = true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        val currentDestination = navController.currentDestination?.id

        when (currentDestination) {
            R.id.loginFragment -> {
                finishAffinity()
            }

            R.id.homeFragment -> {
                // Close app when on home screen
                finishAffinity()
            }

            else -> {
                if (!navController.popBackStack()) {
                    finishAffinity()
                }
            }
        }
    }
}