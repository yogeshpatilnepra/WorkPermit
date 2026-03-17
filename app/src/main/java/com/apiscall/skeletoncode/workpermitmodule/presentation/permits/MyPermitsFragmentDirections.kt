package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.apiscall.skeletoncode.R

class MyPermitsFragmentDirections private constructor() {

    data class ActionMyPermitsFragmentToPermitDetailsFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_myPermitsFragment_to_permitDetailsFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }

    data class ActionMyPermitsFragmentToCreatePermitFragment : NavDirections {
        override fun getActionId(): Int = R.id.action_myPermitsFragment_to_createPermitFragment
        override fun getArguments(): Bundle = bundleOf()
    }
}