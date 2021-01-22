package com.words.storageapp.util

interface Storage {
    fun getString(key: String): String
    fun setString(key: String, value: String)
}