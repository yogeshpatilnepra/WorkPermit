package com.apiscall.skeletoncode.workpermitmodule.di

import androidx.lifecycle.ViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.auth.LoginViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.home.HomeViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitListViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitDetailViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.CreatePermitViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.DynamicFormViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels.PermitClosureViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.attachments.AttachmentViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.notifications.NotificationsViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.profile.ProfileViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.qr.QRViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.search.SearchViewModel
import com.apiscall.skeletoncode.workpermitmodule.presentation.worker.WorkerViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @ViewModelScoped
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindPermitListViewModel(permitListViewModel: PermitListViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindPermitDetailViewModel(permitDetailViewModel: PermitDetailViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindCreatePermitViewModel(createPermitViewModel: CreatePermitViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindDynamicFormViewModel(dynamicFormViewModel: DynamicFormViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindPermitClosureViewModel(permitClosureViewModel: PermitClosureViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindAttachmentViewModel(attachmentViewModel: AttachmentViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindNotificationsViewModel(notificationsViewModel: NotificationsViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindQRViewModel(qrViewModel: QRViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindWorkerViewModel(workerViewModel: WorkerViewModel): ViewModel
}