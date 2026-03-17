package com.apiscall.skeletoncode.workpermitmodule

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PTWApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize any app-wide configurations here
    }
}