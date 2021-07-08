package com.words.storageapp.ui.main.di

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.di.SharedPreference
import com.words.storageapp.di.Storage
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.utilities.ConnectivityChecker
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class MainModule {

    @Binds
    abstract fun context(activity: MainActivity): Context

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideAppDatabase(context: Context): AppDatabase = AppDatabase.getInstance(context)

        @JvmStatic
        @Provides
        fun connectivityChecker(activity: Activity): ConnectivityChecker? {
            val connectivityManager = activity.getSystemService<ConnectivityManager>()
            return if (connectivityManager != null) {
                ConnectivityChecker(connectivityManager)
            } else {
                null
            }
        }
    }

}