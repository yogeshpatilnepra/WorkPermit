package com.apiscall.skeletoncode.workpermitmodule.data.local.database

import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apiscall.skeletoncode.workpermitmodule.data.local.database.dao.PermitDao
import com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities.PermitEntity
import com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities.UserEntity


@Database(
    entities = [PermitEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PermitDatabase : RoomDatabase() {
    abstract fun permitDao(): PermitDao
}