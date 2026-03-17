package com.apiscall.skeletoncode.workpermitmodule.domain.usecases.permit


import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import javax.inject.Inject

class CreatePermitUseCase @Inject constructor(
    private val permitRepository: PermitRepository
) {
    suspend operator fun invoke(permit: Permit): Result<Permit> {
        return permitRepository.createPermit(permit)
    }
}