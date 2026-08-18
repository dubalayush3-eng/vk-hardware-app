package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

/**
 * Custom [Application] class that provides a centralized Dependency Injection
 * container throughout the application lifecycle.
 */
class HardwareApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
