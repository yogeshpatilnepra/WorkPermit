package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.apiscall.skeletoncode.R

class PermitDetailsFragmentDirections private constructor() {

    data class ActionPermitDetailsFragmentToUploadAttachmentFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_permitDetailsFragment_to_uploadAttachmentFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }

    data class ActionPermitDetailsFragmentToWorkerSignInFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_permitDetailsFragment_to_workerSignInFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }

    data class ActionPermitDetailsFragmentToPermitClosureFragment(
        val permitId: String
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_permitDetailsFragment_to_permitClosureFragment
        override fun getArguments(): Bundle = bundleOf("permitId" to permitId)
    }
}