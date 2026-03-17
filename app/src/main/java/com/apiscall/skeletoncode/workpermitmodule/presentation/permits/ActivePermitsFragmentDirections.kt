package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.apiscall.skeletoncode.R

class ActivePermitsFragmentDirections private constructor() {

    data class ActionActivePermitsFragmentToPermitDetailsFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_activePermitsFragment_to_permitDetailsFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }

    data class ActionActivePermitsFragmentToWorkerSignInFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_activePermitsFragment_to_workerSignInFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }

    data class ActionActivePermitsFragmentToWorkerSignOutFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_activePermitsFragment_to_workerSignOutFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }
}