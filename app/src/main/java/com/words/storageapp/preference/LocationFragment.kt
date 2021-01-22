package com.words.storageapp.preference

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.adapters.AddressClickListener
import com.words.storageapp.adapters.AddressListAdapter
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.AddressModel
import com.words.storageapp.database.model.AllSkillsDbModel
import com.words.storageapp.databinding.FragmentLocationBinding
import com.words.storageapp.domain.RegisterUser
import com.words.storageapp.domain.toWokrData
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.utilities.ConnectivityChecker
import com.words.storageapp.util.Constants
import com.words.storageapp.util.Constants.ADDRESS_KEY
import com.words.storageapp.util.Constants.ADDRESS_REQUESTED
import com.words.storageapp.util.utilities.FetchAddressIntentService
import kotlinx.android.synthetic.main.fragment_profile2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

class LocationFragment : Fragment() {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 111
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private lateinit var geoFirestore: GeoFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var collectionRef: CollectionReference //reference for firestore
    private lateinit var dataReference: DatabaseReference
    private lateinit var skillReference: DatabaseReference

    @Inject
    lateinit var db: AppDatabase

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
    private val skillsData = mutableListOf<AllSkillsDbModel>()

    private lateinit var noNetworkView: ConstraintLayout
    private lateinit var loadingView: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        setLocationRequest()
        locationCallback()
        addressResultReceiver = AddressResultReceiver(Handler())
        firebaseAuth = FirebaseAuth.getInstance()
        collectionRef = FirebaseFirestore.getInstance()
            .collection("skills")
        geoFirestore = GeoFirestore(collectionRef)

        dataReference = Firebase.database.reference
        skillReference = dataReference.child("skills")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_splash, container, false)
        val binding = FragmentLocationBinding.inflate(inflater, container, false)

        sharedPreferences = (activity as MainActivity).sharedPref
        networkConnection = (activity as MainActivity).connectivityChecker
        noNetworkView = binding.noNetwork
        loadingView = binding.loadingLayout
        setUpOnBackPressedCallback()

        binding.fetchAddress.setOnClickListener {
            getAddress()
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
    }

    override fun onResume() {
        super.onResume()
        val addressRequested = sharedPreferences.getBoolean(
            ADDRESS_REQUESTED,
            false
        )
        if (!addressRequested) {
            setUpUI()
        }
    }

    private fun setUpUI() {
        networkConnection?.apply {
            lifecycle.addObserver(this)
            connectedStatus.observe(viewLifecycleOwner, Observer {
                if (it) {
                    hideNoInternet()
                    createLocationRequest()
                } else {
                    showNoInternet()
                }
            })
        } ?: showNoInternet()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

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

    @SuppressLint("MissingPermission")
    private fun getAddress() {
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener(
            requireActivity(),
            OnSuccessListener { location ->
                if (location == null) {
                    Timber.i("onSuccess::null")
                    return@OnSuccessListener
                }
                lastLocation = location

                Toast.makeText(
                    requireContext(), "latitude: ${location.latitude}" +
                            ", longitude: ${location.longitude}", Toast.LENGTH_SHORT
                ).show()

                if (!Geocoder.isPresent()) {
                    Timber.i("OnSuccess::GeoCoder is not present")
                    return@OnSuccessListener
                }
                //if address is available don't' find address again
                startIntentService()
            })
    }


    private fun navigateToHomeFragment(address: String) {
        storeLocationDetails()
        val bundle = bundleOf(ADDRESS_KEY to address)
        findNavController().navigate(R.id.homeFragment, bundle)
    }

    private fun storeLocationDetails() {
        with(sharedPreferences.edit()) {
            putString(ADDRESS_KEY, addressOutput)
            putBoolean(ADDRESS_REQUESTED, addressRequested)
            commit()
        }
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
                Toast.makeText(
                    requireContext(), "localities: $locality," +
                            "addresses: $addresses"
                    , Toast.LENGTH_SHORT
                ).show()

                lifecycleScope.launch(Dispatchers.IO) {
                    //db.addressDao().deleteAddresses()
                    addresses?.forEach { address ->
                        val addressModel = AddressModel(
                            address,
                            locality?.get(0),
                            lastLocation!!.latitude,
                            lastLocation!!.longitude
                        )
                        db.addressDao().insertAddress(addressModel)
                    }
                }
                if (addresses != null) {
                    val bundle = bundleOf("ADDRESSES" to addresses)
                    findNavController().navigate(R.id.addressDialogFragment, bundle)
                }
                cacheLocality(locality?.get(0))
            }
        }
    }

    fun cacheLocality(locality: String?) {
        with(sharedPreferences.edit()) {
            putString("LOCALITY", locality)
            commit()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        //check if location permission is accepted first
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest
            , locationCallback, Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startIntentService() {
        val intent = Intent(requireContext(), FetchAddressIntentService::class.java).apply {
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
                    if (location != null &&
                        !addressRequested && !requestingAddress
                    ) {
//                    if(location != null) {
                        requestingAddress = true
                        latitude = location.latitude
                        longitude = location.longitude
                        getAddress()
                        initDBFromFirebase()
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

    private fun fetchFromLocation(lat: Double, long: Double) {
        val queryCenter = GeoPoint(lat, long)

        Toast.makeText(
            requireContext(),
            "Lat: $lat, Lon: $long", Toast.LENGTH_LONG
        ).show()

        Timber.i("splashFragment FetchFromLocation called:")

        geoFirestore.getAtLocation(queryCenter, 600.0) { docs, ex ->
            if (ex != null) {
                Timber.e(ex, "Unable to fetch document")
                return@getAtLocation
            } else {
                docs?.let { skills ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        for (result in skills) {
                            val registerUser = result.toObject(RegisterUser::class.java)
                            db.allSkillsDbDao().insert(registerUser!!.toWokrData())
                            Timber.i("Users : $registerUser")
                        }
                        withContext(Dispatchers.Main) {
                            navigateToHomeFragment(addressOutput!!)
                        }
                    }
                }
            }
        }
    }


    fun initDBFromFirebase() {
        skillReference.get().addOnSuccessListener {
            it.children.mapNotNullTo(skillsData) { data ->
                data.getValue(RegisterUser::class.java)?.toWokrData()
            }.also {
                Toast.makeText(requireContext(), "skills: $skillsData", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch(Dispatchers.IO) {
                    db.allSkillsDbDao().insertAll(skillsData)
                }
            }
        }
    }

    fun addressListDialog(addressList: ArrayList<String>) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.address_list))
            val inflater = LayoutInflater.from(requireContext())
            val view = inflater.inflate(R.layout.address_list_layout, null)

            val addressRecyclerView = view.findViewById<RecyclerView>(R.id.addressList)
            val adapter = AddressListAdapter(addressList, AddressClickListener {
                Toast.makeText(requireContext(), "Items Clicked", Toast.LENGTH_SHORT).show()
                val action = R.id.action_locationFragment_to_homeFragment
                findNavController().navigate(action)
            })
            addressRecyclerView.adapter = adapter
            setCancelable(false)
            show()
        }
    }

    private fun showNoInternet() {
        noNetworkView.visibility = View.VISIBLE
    }

    private fun hideNoInternet() {
        noNetworkView.visibility = View.GONE
    }

    private fun showLoading() {
        loadingView.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingView.visibility = View.GONE
    }

}