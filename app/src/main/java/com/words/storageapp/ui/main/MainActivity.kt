package com.words.storageapp.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.MyApplication
import com.words.storageapp.R
import com.words.storageapp.authentication.PersonDetail
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.AddressModel
import com.words.storageapp.di.dagger.AppLevelComponent
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.domain.toAllSkillsModel
import com.words.storageapp.ui.UserManager
import com.words.storageapp.util.Constants.ADDRESS_REQUESTED
import com.words.storageapp.util.utilities.ConnectivityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_LOCATION_CODE = 10930
    }

    var connectivityChecker: ConnectivityChecker? = null
    val registerModel = PersonDetail()

    lateinit var daggerAppLevelComponent: AppLevelComponent
    private lateinit var navController: NavController
    private lateinit var databaseReference: DatabaseReference
    private lateinit var skillDatabase: DatabaseReference
    private lateinit var skillListener: ValueEventListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isListening: Boolean = false

    val sharedPref: SharedPreferences by lazy {
        this.getPreferences(Context.MODE_PRIVATE)
    }

    private lateinit var bottomNav: BottomNavigationView

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var db: AppDatabase

    //this method initializes the main screen
    override fun onCreate(savedInstanceState: Bundle?) {
        daggerAppLevelComponent = (application as MyApplication).daggerComponent
        daggerAppLevelComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.mainNavHost)
        databaseReference = Firebase.database.reference
        skillDatabase = databaseReference.child("skills")
        setListener()

        bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        setUpBottomNav(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (
                destination.id == R.id.searchFragment ||
                destination.id == R.id.skilledFragment2 ||
                destination.id == R.id.locationFragment ||
                destination.id == R.id.editAlbumFragment ||
                destination.id == R.id.profileImageFragment ||
                destination.id == R.id.onboardingFragment ||
                destination.id == R.id.skillFragment ||
                destination.id == R.id.adminFragment ||
                destination.id == R.id.admin_skill_fragment ||
                destination.id == R.id.processFragment ||
                destination.id == R.id.paymentFragment ||
                destination.id == R.id.fullPhotoFragment ||
                destination.id == R.id.registerFragment ||
                destination.id == R.id.clientRegistration ||
                destination.id == R.id.loginFragment ||
                destination.id == R.id.skillsPagerFragment ||
                destination.id == R.id.preferenceFragment ||
                destination.id == R.id.configureFragment
            ) {
                bottomNav.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
            }
        }
        connectivityChecker = connectivityChecker(this)
    }

    /***********************************************************************************************/
    //setting up bottom navigation
    private fun setUpBottomNav(navController: NavController) {
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)
    }

    private fun connectivityChecker(activity: Activity): ConnectivityChecker? {
        val connectivityManager = activity.getSystemService<ConnectivityManager>()
        return if (connectivityManager != null) {
            ConnectivityChecker(connectivityManager)
        } else {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        val isAddressAval = sharedPref.getBoolean(ADDRESS_REQUESTED, true)
        //consider putting this if condition inside the network section
        if (!isAddressAval) {
            connectivityChecker?.apply {
                lifecycle.addObserver(this)
                connectedStatus.observe(this@MainActivity, Observer {
                    if (it) {

                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        connectivityChecker?.apply {
            lifecycle.addObserver(this)
            connectedStatus.observe(this@MainActivity, Observer {
                if (it && !isListening) {
                    skillDatabase.addValueEventListener(skillListener)
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        //skillDatabase.addValueEventListener(skillListener)
        stopListening()
    }

    private fun setListener() {
        isListening = true
        skillListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { data ->
                    data.getValue(FirebaseUser::class.java)?.toAllSkillsModel()
                }.also {
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.allSkillsDbDao().insertAll(it)
                    }
                }
            }
        }
    }

    private fun stopListening() {
        skillDatabase.removeEventListener(skillListener)
    }

    fun hideKeyBoard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyBoard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }
}
