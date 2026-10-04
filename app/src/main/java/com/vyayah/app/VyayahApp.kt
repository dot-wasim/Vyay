package com.vyayah.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vyayah.app.di.appModule
import com.vyayah.app.worker.CatchUpSyncWorker
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class VyayahApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize SQLCipher native libraries
        SQLiteDatabase.loadLibs(this)

        // Initialize Koin Dependency Injection
        startKoin {
            androidLogger()
            androidContext(this@VyayahApp)
            modules(appModule)
        }

        // Schedule periodic catch-up sync (safety net against aggressive OEM task killers)
        schedulePeriodicCatchUpSync()
    }

    private fun schedulePeriodicCatchUpSync() {
        val syncRequest = PeriodicWorkRequestBuilder<CatchUpSyncWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CatchUpSyncWorkerPeriodic",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
