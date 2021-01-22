package com.words.storageapp.di.dagger

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides


/*
*  Provide sharedPreference for app's component
* */
@Module
class SharedPreferenceModule(val context: Context, val name: String) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}