package com.words.storageapp.di

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.work.workDataOf
import javax.inject.Inject

class SharedPreference @Inject constructor(context: Context) : Storage {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("Wokr", Context.MODE_PRIVATE)

    override fun getLocationProperty(key: String): String {
        return sharedPref.getString(key, "")!!
    }

    override fun setLocationProperty(key: String, value: String) {
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }


    override fun getLocationState(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    override fun setLocationState(key: String, value: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }


}