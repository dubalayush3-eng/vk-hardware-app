package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.HardwareRepository

/**
 * Dependency Injection container interface for application-wide dependencies.
 */
interface AppContainer {
    val database: AppDatabase
    val hardwareRepository: HardwareRepository
}

/**
 * Default implementation of [AppContainer] that lazily initializes the
 * Room database instance and [HardwareRepository].
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val hardwareRepository: HardwareRepository by lazy {
        HardwareRepository(database)
    }
}
