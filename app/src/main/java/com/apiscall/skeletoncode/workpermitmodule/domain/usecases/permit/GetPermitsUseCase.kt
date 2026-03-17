package com.apiscall.skeletoncode.workpermitmodule.domain.usecases.permit

import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPermitsUseCase @Inject constructor(
    private val permitRepository: PermitRepository
) {
    suspend operator fun invoke(): Flow<List<Permit>> = permitRepository.getPermits()
}