package com.words.storageapp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.words.storageapp.di.dagger.AppLevelComponent
import com.words.storageapp.di.dagger.DaggerAppLevelComponent
import timber.log.Timber

open class MyApplication : Application() {


    //whenever you're passing context as argument declare the variable as lazy
    //so that it can only be  called in the onCreate method of An Activity.
    val daggerComponent: AppLevelComponent by lazy {
        DaggerAppLevelComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    //here we fix the index memory compile time error message
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}