package com.words.storageapp

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.multidex.MultiDex
import androidx.work.*
import com.words.storageapp.di.dagger.AppLevelComponent
import com.words.storageapp.di.dagger.DaggerAppLevelComponent
import com.words.storageapp.work.BackgroundPrefetchSkill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

open class MyApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    //whenever you're passing context as argument declare the variable as lazy
    //so that it can only be  called in the onCreate method of An Activity.
    val daggerComponent: AppLevelComponent by lazy {
        DaggerAppLevelComponent.factory().create(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        initiateWorkers()
    }

    //here we fix the index memory compile time error message
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun initiateWorkers() {
        applicationScope.launch {
            runPrefetchWorker()
        }
    }

    private fun runPrefetchWorker() {

        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val repeatingRequest =
            PeriodicWorkRequestBuilder<BackgroundPrefetchSkill>(
                1,
                TimeUnit.DAYS
            ).setConstraints(constraint)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            BackgroundPrefetchSkill.Work_Name,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )

    }
}