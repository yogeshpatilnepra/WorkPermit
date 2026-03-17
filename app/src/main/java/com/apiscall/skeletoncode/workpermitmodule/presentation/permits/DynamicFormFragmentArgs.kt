package com.apiscall.skeletoncode.workpermitmodule.presentation.permits


import android.os.Bundle
import androidx.navigation.NavArgs
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType

class DynamicFormFragmentArgs private constructor(
    val permitType: PermitType
) : NavArgs {

    fun toBundle(): Bundle {
        val result = Bundle()
        result.putSerializable("permitType", permitType)
        return result
    }

    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): DynamicFormFragmentArgs {
            bundle.classLoader = PermitType::class.java.classLoader
            return DynamicFormFragmentArgs(
                bundle.getSerializable("permitType") as PermitType
            )
        }
    }
}