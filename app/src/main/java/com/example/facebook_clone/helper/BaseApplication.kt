package com.example.facebook_clone.helper

import android.app.Application
import com.example.facebook_clone.di.databaseModule
import com.example.facebook_clone.di.firebaseAuthModule
import com.example.facebook_clone.di.firebaseStorageModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    databaseModule,
                    firebaseAuthModule,
                    firebaseStorageModule
                )
            )
        }
    }
}