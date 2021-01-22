package com.words.storageapp.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.databinding.FragmentHomeBinding
import com.words.storageapp.domain.RegisterUser
import com.words.storageapp.domain.toWokrData
import com.words.storageapp.util.utilities.ConnectivityChecker
import com.words.storageapp.util.Constants.ADDRESS_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import timber.log.Timber

class HomeFragment : Fragment() {

    private val args: String? by lazy {
        arguments?.getString(ADDRESS_KEY)
    }

    private val connectionChecker: ConnectivityChecker? by lazy {
        (activity as MainActivity).connectivityChecker
    }

    private lateinit var collectionRef: CollectionReference
    private lateinit var geoFirestore: GeoFirestore
    lateinit var callback: OnBackPressedCallback

    //@Inject
    private lateinit var appDatabase: AppDatabase

    private lateinit var fireDatabase: DatabaseReference
    private lateinit var skillsReference: DatabaseReference
    private lateinit var geoFire: GeoFire
    lateinit var geoQuery: GeoQuery
    private lateinit var dataListener: GeoQueryDataEventListener


    private lateinit var progressBar: ProgressBar

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectionRef =
            FirebaseFirestore.getInstance().collection(getString(R.string.fireStore_node))
        geoFirestore = GeoFirestore(collectionRef)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        appDatabase = (activity as MainActivity).db

        fireDatabase = Firebase.database.reference
        skillsReference = fireDatabase.child("skills")
        geoFire = GeoFire(skillsReference)

        if (!checkLocationPermissionApproved()) {
            startLocationPermission()
        }
    }

    override fun onAttach(context: Context) {
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.searchBtn.setOnClickListener {
            val action = R.id.action_homeFragment_to_searchFragment
            findNavController().navigate(action)
        }

        progressBar = binding.homeProgress
        sharedPreferences = (activity as MainActivity).sharedPref

        val address = sharedPreferences.getString(ADDRESS_KEY, null)
        if (address == null) {
            val action = R.id.action_homeFragment_to_onBoardingFragment
            findNavController().navigate(action)
        }

        Toast.makeText(
            requireContext(),
            "Address: $address ", Toast.LENGTH_SHORT
        ).show()
        binding.addressView.text = address

        setUpOnBackPressedCallback()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        return binding.root
    }

    private fun setUpOnBackPressedCallback() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).finish()
            }
        }
    }

    private fun initializeNetwork() {
        connectionChecker?.apply {
            lifecycle.addObserver(this)
            connectedStatus.observe(viewLifecycleOwner, Observer<Boolean> { _ ->
            })
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

    private fun cacheDataCompleted() {
        with(sharedPreferences.edit()) {
            putBoolean("DONE", true)
            commit()
        }
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

}
