package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.os.Bundle
import androidx.navigation.NavArgs

class PermitClosureFragmentArgs private constructor(
    val permitId: String
) : NavArgs {

    fun toBundle(): Bundle {
        val result = Bundle()
        result.putString("permitId", permitId)
        return result
    }

    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): PermitClosureFragmentArgs {
            return PermitClosureFragmentArgs(
                bundle.getString("permitId") ?: ""
            )
        }
    }
}