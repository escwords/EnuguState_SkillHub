package com.words.storageapp.di

import java.util.ArrayList

interface Storage {
    fun getLocationProperty(key: String): String
    fun setLocationProperty(key: String, value: String)
    fun getLocationState(key: String): Boolean
    fun setLocationState(key: String, value: Boolean)

//    fun setLocationAddress(key: String,value: ArrayList<String>)
//    fun getLocationAddress(key: String)
}