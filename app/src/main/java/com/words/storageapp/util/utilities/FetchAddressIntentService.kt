package com.words.storageapp.util.utilities

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import com.words.storageapp.util.Constants
import timber.log.Timber
import java.io.IOException
import java.util.*


class FetchAddressIntentService : IntentService("FetchAddress") {

    private var receiver: ResultReceiver? = null

    @SuppressLint("TimberExceptionLogging")
    override fun onHandleIntent(intent: Intent?) {

        var errorMessage = ""

        receiver = intent?.getParcelableExtra(Constants.RECEIVER)

        if (intent == null || receiver == null) {
            Timber.i("No Receiver Received")
            return
        }

        val location = intent.getParcelableExtra<Location>(Constants.LOCATION_DATA_EXTRA)

        if (location == null) {
            errorMessage = "no location data provided"
            deliverResultToReceiver(
                Constants.FAILURE_RESULT,
                arrayListOf(errorMessage), arrayListOf(errorMessage)
            )
            return
        }

        val geoCoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address> = emptyList()

        try {
            addresses = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                4
            )
        } catch (ioException: IOException) {
            //catch network or other I/O problems.
            errorMessage = "Service not Available"
            Timber.e(ioException, errorMessage)
        } catch (illegalArgumentException: IllegalArgumentException) {
            //catch invalid latitude or longitude
            errorMessage = "Invalid latitude and Longitude"
            Timber.e(illegalArgumentException, errorMessage)
        }

        //handle where no address is found
        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found"
            }
            deliverResultToReceiver(
                Constants.FAILURE_RESULT, arrayListOf(errorMessage),
                arrayListOf(errorMessage)
            )

        } else {

            val addressCodes = addresses
            val addressList = arrayListOf<String>()
            val localityList = arrayListOf<String>()

            addressCodes.forEach { addressCode ->
                val resultAddress = with(addressCode) {
                    (0..maxAddressLineIndex).map { getAddressLine(it) }
                }
                addressList.add(resultAddress.joinToString(separator = "\n"))
            }

//            for (i in 0..addressCodes[0].maxAddressLineIndex) {
//                localityList.add(addressCodes[0].locality)
//            }

//            addressCodes.forEach { addressCode ->
//                val resultAddress = with(addressCode) {
//                    (0..maxAddressLineIndex).map { locality }
//                }
//                localityList.add(resultAddress.joinToString(separator = "\n"))
//            }


            val resultAddress = with(addressCodes[0]) {
                (0..maxAddressLineIndex).map { locality }
            }
            localityList.add(resultAddress.joinToString(separator = "\n"))


            Timber.i("Address Found: $addressList")
            deliverResultToReceiver(
                Constants.SUCCESS_RESULT,
                addressList,
                localityList
            )
        }
    }

    private fun deliverResultToReceiver(
        resultCode: Int, addresses: ArrayList<String>,
        locality: ArrayList<String>
    ) {
        val bundle1 = Bundle().apply { putStringArrayList(Constants.RESULT_DATA_KEY, addresses) }
        val bundle2 = Bundle().apply { putStringArrayList(Constants.RESULT_LOCALITY, locality) }
        receiver?.send(resultCode, bundle1)
        receiver?.send(resultCode, bundle2)
    }

}