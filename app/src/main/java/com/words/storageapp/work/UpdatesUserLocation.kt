package com.words.storageapp.work

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationProvider
import android.os.Looper
import android.provider.Settings
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import timber.log.Timber
import java.util.concurrent.TimeUnit

class UpdatesUserLocation(context: Context, val sharedPref: SharedPreferences) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest().apply {
        interval = TimeUnit.SECONDS.toMillis(1000)
        fastestInterval = TimeUnit.SECONDS.toMillis(2000)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if ((locationResult?.lastLocation != null)) {

                //Store location object inside the database
            } else {
                Timber.e("Location object is missing")
            }
        }
    }

    private fun subscribeToLocation() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {

            Timber.e("Lost location Couldn't remove update. $unlikely")
        }
    }

    private fun unSubscribeToLocation() {

        Timber.i("unsubscribeToLocationUpdates()")

        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("Location Callback removed.")
                    } else {
                        Timber.d("Failed to remove Location Callback.")
                    }
                }
            //SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            //SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

}