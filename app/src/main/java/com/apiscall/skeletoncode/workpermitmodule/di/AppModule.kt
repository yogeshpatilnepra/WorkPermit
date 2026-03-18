package com.apiscall.skeletoncode.workpermitmodule.di

import android.content.Context
import androidx.room.Room
import com.apiscall.skeletoncode.workpermitmodule.data.local.database.PermitDatabase
import com.apiscall.skeletoncode.workpermitmodule.data.local.datasource.MockDataSource
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import dagger.Module
import dagger.Provides
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMockDataSource(): MockDataSource {
        return MockDataSource()
    }

    @Provides
    @Singleton
    fun providePermitDatabase(@ApplicationContext context: Context): PermitDatabase {
        return Room.databaseBuilder(
            context,
            PermitDatabase::class.java,
            "permit_database"
        ).build()
    }
}