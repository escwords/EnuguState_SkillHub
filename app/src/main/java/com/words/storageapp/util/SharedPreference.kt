package com.words.storageapp.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(context: Context) : Storage {

    private val sharedPreference: SharedPreferences =
        context.getSharedPreferences("UserLocation", Context.MODE_PRIVATE)

    override fun getString(key: String): String {
        return sharedPreference.getString(key, "")!!
    }

    override fun setString(key: String, value: String) {
        with(sharedPreference.edit()) {
            putString(key, value)
            apply()
        }
    }
}