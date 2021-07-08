package com.words.storageapp.ui

import com.words.storageapp.di.Storage
import com.words.storageapp.util.CurrentLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val storage: Storage
) {

    //val uAddress: String = storage.getLocationProperty("Address")
    val uLongitude: Double? = storage.getLocationProperty("latitude").toDoubleOrNull()
    val uLatitude: Double? = storage.getLocationProperty("longitude").toDoubleOrNull()
    val locationState: Boolean = storage.getLocationState("Status")
    // var locationMain: Location? = null

    fun setUpLocation(location: CurrentLocation) {
        storage.setLocationProperty("latitude", location.latitude.toString())
        storage.setLocationProperty("longitude", location.longitude.toString())
        // locationMain = location
    }

    fun setLocationState(value: Boolean) {
        storage.setLocationState("Status", value)
    }

}