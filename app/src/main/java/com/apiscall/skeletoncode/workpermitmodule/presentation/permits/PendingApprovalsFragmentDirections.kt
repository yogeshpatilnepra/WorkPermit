package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.apiscall.skeletoncode.R

class PendingApprovalsFragmentDirections private constructor() {

    data class ActionPendingApprovalsFragmentToPermitDetailsFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int =
            R.id.action_pendingApprovalsFragment_to_permitDetailsFragment

        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }
}