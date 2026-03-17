package com.apiscall.skeletoncode.workpermitmodule.domain.usecases.permit


import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import javax.inject.Inject

class ApprovePermitUseCase @Inject constructor(
    private val permitRepository: PermitRepository
) {
    suspend operator fun invoke(permitId: String, comments: String?): Result<Permit> {
        return permitRepository.approvePermit(permitId, comments)
    }
}