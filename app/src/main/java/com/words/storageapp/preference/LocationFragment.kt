package com.words.storageapp.preference

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.AddressModel
import com.words.storageapp.databinding.FragmentLocationBinding
import com.words.storageapp.domain.*
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.utilities.ConnectivityChecker
import com.words.storageapp.util.Constants
import com.words.storageapp.util.Constants.ADDRESS_REQUESTED
import com.words.storageapp.util.utilities.FetchAddressIntentService
import com.words.storageapp.work.BackgroundPrefetchSkill
import com.words.storageapp.work.SeedDatabaseWorker
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocationFragment : Fragment() {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 111
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var skillDbPath: DatabaseReference
    private var network: Boolean = true

    @Inject
    lateinit var localDb: AppDatabase

    //store retrieved address
    var addressOutput: String? = ""
    var addressRequested: Boolean = false
    var requestingAddress: Boolean = false

    private var lastLocation: Location? = null
    private lateinit var addressResultReceiver: AddressResultReceiver
    private lateinit var sharedPreferences: SharedPreferences
    private var networkConnection: ConnectivityChecker? = null
    lateinit var callback: OnBackPressedCallback
    private var latitude = 0.2
    private var longitude = 0.2

    private lateinit var loadingView: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        setLocationRequest()
        locationCallback()
        addressResultReceiver = AddressResultReceiver(Handler())
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDb = Firebase.database.reference
        skillDbPath = firebaseDb.child("skills")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLocationBinding.inflate(inflater, container, false)

        sharedPreferences = (activity as MainActivity).sharedPref
        networkConnection = (activity as MainActivity).connectivityChecker
        loadingView = binding.loadingLayout
        val searchImage = binding.searchingIcon
        animateWarning(searchImage)
        setUpOnBackPressedCallback()

        binding.fetchAddress.setOnClickListener {
            navigateToStartScreen()
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        return binding.root
    }


    /*************************************
     * we setup dialog fragment here
     *************************************/
    private fun setUpOnBackPressedCallback() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!checkLocationPermissionApproved()) {
            startLocationPermission()
        }
        setUpUI()
    }

    override fun onResume() {
        super.onResume()
        val addressRequested = sharedPreferences.getBoolean(
            ADDRESS_REQUESTED,
            false
        )
        //In case user cancel while the location finding is going on
        if (!addressRequested) {
            setUpUI()
        }
    }


    private fun setUpUI() {
        networkConnection?.apply {
            lifecycle.addObserver(this)
            connectedStatus.observe(viewLifecycleOwner, Observer {
                network = it
            })
        }
        if (network) {
            createLocationRequest()
            //navigateToStartScreen()
        } else {
            hideLoading()
            noLocationDialog()
            //navigateToStartScreen()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    //creating location request
    private fun createLocationRequest() {

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(builder?.build())

        task.addOnSuccessListener {
            lifecycleScope.launch {
                showLoading()
            }
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.i("Cannot resolve location Error")
                }
            }
        }
    }

    //caching the state of onboarding screen
    private fun onBoarding() {
        with(sharedPreferences.edit()) {
            putBoolean("OnBoarding", true)
            commit()
        }
    }

    //this function animates the location icon
    private fun animateWarning(icon: ImageView) {
        val prevColor = Color.parseColor("#E6FBFFFF")
        val newColor = Color.parseColor("#EC2020")
        ValueAnimator.ofObject(ArgbEvaluator(), newColor, prevColor).apply {
            repeatCount = 1000
            duration = 1000
            addUpdateListener { valueAnimator ->
                val background = valueAnimator.animatedValue as Int
                icon.setBackgroundColor(background)
                icon.setColorFilter(Color.WHITE)
            }
            start()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getAddress() {
        Timber.i("getAddress called")
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener(
            requireActivity(),
            OnSuccessListener { location ->
                if (location == null) {
                    Timber.i("onSuccess::null")
                    return@OnSuccessListener
                }
                lastLocation = location
                lifecycleScope.launch(Dispatchers.IO) {
                    val addressModel = AddressModel(
                        lastLocation!!.latitude,
                        lastLocation!!.longitude
                    )
                    localDb.addressDao().insertAddress(addressModel)
                    storeLocation()
                }
                lifecycleScope.launchWhenResumed {
                    delay(10000)
                    hideLoading()
                    timeOutError()
                }

                if (!Geocoder.isPresent()) {
                    Timber.i("OnSuccess::GeoCoder is not present")
                    addressNoAvailable()
                    return@OnSuccessListener
                }
                //if address is available don't' find address again
                startIntentService()
            })
    }

    private inner class AddressResultReceiver internal constructor(
        handler: Handler
    ) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
//            addressOutput = resultData?.getString(Constants.RESULT_DATA_KEY)
            val addresses = resultData?.getStringArrayList(Constants.RESULT_DATA_KEY)
            val locality = resultData?.getStringArrayList(Constants.RESULT_LOCALITY)

            if (resultCode == Constants.SUCCESS_RESULT) {
                addressOutput = addresses?.get(1)

                Timber.i("localities: $locality , address:$addresses[1]")

                if (addresses != null) {
                    hideLoading()
                    addressAvailable(addresses[0])     //dialog to navigate to start screen
                    storeAddress()
                    lifecycleScope.launch {
                        delay(1000)
                        navigateToStartScreen()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        //check if location permission is accepted first
        if (checkLocationPermissionApproved()) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startIntentService() {
        val intent = Intent(
            requireContext(),
            FetchAddressIntentService::class.java
        ).apply {
            putExtra(Constants.RECEIVER, addressResultReceiver)
            putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation)
        }
        requireActivity().startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Timber.i("user accept location permission")
                    }
                    Activity.RESULT_CANCELED -> {
                        Timber.i("User canceled location request")
                    }
                }
            }
        }
    }

    private fun locationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    //check if address is available if available don't call getAddress
                    if (
                        location != null &&
                        !addressRequested &&
                        !requestingAddress
                    ) {
                        requestingAddress = true
                        latitude = location.latitude
                        longitude = location.longitude
                        getAddress()
                    }
                }
            }
        }
    }

    private fun setLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 60000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun checkLocationPermissionApproved() = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun startLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            , MainActivity.REQUEST_LOCATION_CODE
        )
    }

    private fun timeOutError() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Oops Time Out!!")
            setMessage("Issue with your internet Connection. \nDo you want to Continue with offline Data?")

            setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                navigateToStartScreen()
                //loadDataFromAsset()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            show()
        }
    }

    private fun addressNoAvailable() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Oops Cannot Find your address!!")
            setMessage("But we found your Location. \nDo you want to continue without address?")
        }
    }

    private fun addressAvailable(address: String) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("We Found your address!!")
            setMessage(address)
            show()
        }
    }

    private fun noLocationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .apply {
                setTitle("OOps!! cannot find your Location.")
                setMessage(
                    "Check your internet Connection " +
                            "\nDo you want to Continue with Default Location?"
                )
                setPositiveButton("Continue") { dialog, _ ->
                    dialog.dismiss()
                    navigateToStartScreen()
                }
                setNegativeButton("Retry") { dialog, _ ->
                    dialog.dismiss()
                    getAddress()
                }
                show()
            }
    }

    private fun firebaseError() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Oops cannot fetch Data!!")
            setMessage("Check your internet Connection \nDo you want to Continue with offline Data?")
            setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                navigateToStartScreen()
            }
            setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                //prefetchFromFirebase()
                getAddress()
            }
            show()
        }
    }


    //shared pref to cache location request
    private fun storeLocation() {
        with(sharedPreferences.edit()) {
            putBoolean("LOCATION_REQUESTED", true)
            commit()
        }
    }

    private fun storeAddress() {
        with(sharedPreferences.edit()) {
            putBoolean(ADDRESS_REQUESTED, true)
            commit()
        }
    }

    private fun navigateToStartScreen() {
        onBoarding()
        //val action = R.id.action_locationFragment_to_startFragment
        findNavController().navigate(R.id.startFragment)
    }

    private fun showLoading() {
        loadingView.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingView.visibility = View.GONE
    }

}