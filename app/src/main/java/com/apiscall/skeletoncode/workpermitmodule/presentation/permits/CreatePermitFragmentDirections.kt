package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.apiscall.skeletoncode.R
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType


class CreatePermitFragmentDirections private constructor() {

    data class ActionCreatePermitFragmentToDynamicFormFragment(
        val permitType: PermitType
    ) : NavDirections {
        override fun getActionId(): Int = R.id.action_createPermitFragment_to_dynamicFormFragment
        override fun getArguments(): Bundle = bundleOf("permitType" to permitType)
    }
}