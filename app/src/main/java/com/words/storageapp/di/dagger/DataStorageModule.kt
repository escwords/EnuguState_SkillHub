package com.words.storageapp.di.dagger

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
import javax.inject.Singleton

@Module
abstract class DataStorageModule {

    @Singleton
    @Binds
    abstract fun provideSharedPreference(storage: SharedPreference): Storage

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideAppDatabase(context: Context): AppDatabase = AppDatabase.getInstance(context)
    }

}
