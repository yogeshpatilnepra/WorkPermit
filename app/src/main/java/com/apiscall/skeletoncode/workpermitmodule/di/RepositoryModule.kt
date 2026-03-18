package com.apiscall.skeletoncode.workpermitmodule.di


import com.apiscall.skeletoncode.workpermitmodule.data.repository.NotificationRepositoryImpl
import com.apiscall.skeletoncode.workpermitmodule.data.repository.UserRepositoryImpl
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepositoryImpl
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.NotificationRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepositoryImpl
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPermitRepository(
        permitRepositoryImpl: PermitRepositoryImpl
    ): PermitRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}