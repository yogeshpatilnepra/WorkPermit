package com.apiscall.skeletoncode.solarproject.APIS

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface APIComponent {
    fun getRepository(): Repository
}