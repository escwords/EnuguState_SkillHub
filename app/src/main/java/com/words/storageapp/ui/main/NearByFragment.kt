package com.words.storageapp.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.adapters.NearByAdapter2
import com.words.storageapp.adapters.NearByAdapter2.*
import com.words.storageapp.database.model.NearByDbModel
import com.words.storageapp.database.model.toNearBySkill
import com.words.storageapp.databinding.FragmentNearByBinding
import com.words.storageapp.domain.NearBySkill
import com.words.storageapp.domain.RegisterUser
import com.words.storageapp.ui.search.SearchViewModel
import com.words.storageapp.util.USERID
import com.words.storageapp.util.utilities.ConnectivityChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NearByFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var skillsReference: DatabaseReference
    private lateinit var geoQuery: GeoQuery
    private lateinit var dataListener: GeoQueryDataEventListener
    private lateinit var dataReference: DatabaseReference
    private lateinit var collectionRef: CollectionReference
    private lateinit var nearByListener: ValueEventListener

    private lateinit var recyclerView: RecyclerView
    private var connectivityChecker: ConnectivityChecker? = null
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: NearByAdapter2
    private lateinit var nearbyLayout: ViewGroup
    private lateinit var noNetwork: ConstraintLayout
    private lateinit var searchView: SearchView
    private lateinit var titleText: TextView
    private lateinit var resultTitle: TextView

    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var sharedPref: SharedPreferences

    val _result = mutableListOf<NearBySkill>()
    val resultLiveData = MutableLiveData<List<NearBySkill>>()
    val nearBySkillList = mutableListOf<NearByDbModel>()

    @Inject
    lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        fireDatabase = Firebase.database.reference
        skillsReference = fireDatabase.child("skills")
        dataReference = FirebaseDatabase.getInstance().getReference("skills")
        collectionRef = FirebaseFirestore.getInstance().collection("skills")
        setUpNearByListener()
        setUpQueryListener()
        // searchViewModel.nearByQuery("all")
        sharedPref = (activity as MainActivity).sharedPref
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentNearByBinding.inflate(inflater, container, false)

        recyclerView = binding.nearbyRecycler
        progressBar = binding.nearbyProgress
        nearbyLayout = binding.nearLayout
        noNetwork = binding.noNetwork
        searchView = binding.searchBar
        titleText = binding.titleTxt
        resultTitle = binding.listTitle
        connectivityChecker = (activity as MainActivity).connectivityChecker

        progressBar.visibility = View.VISIBLE

        adapter = NearByAdapter2(ClickListener { skill ->
            // Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
            val bundle =
                bundleOf(USERID to skill.id) //call trimIndex on skillId before passing to detail or skilledFragment
            val action = R.id.action_nearByFragment_to_skilledFragment2
            findNavController().navigate(action, bundle)
        })
        setUpListener()
        showProgressBar()
        setUpSearchView()  //sets up search view widget

        searchViewModel.allNearBySkills.observe(viewLifecycleOwner, Observer {
            searchViewModel.nearByQuery("all")
        })

        searchViewModel.nearByFiltered.observe(viewLifecycleOwner, Observer { nearby ->
            //Toast.makeText(requireContext(),"skills: $nearby",Toast.LENGTH_SHORT).show()
            if (nearby.isNullOrEmpty()) {
                hideProgressBar()
                binding.empty.visibility = View.VISIBLE
                binding.emptyTxt.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                hideProgressBar()
                progressBar.visibility = View.GONE
                binding.empty.visibility = View.GONE
                binding.emptyTxt.visibility = View.GONE
                adapter.submitList(nearby)
                cacheDataStatue()
            }
        })

        recyclerView.adapter = adapter
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dataAvailable = sharedPref.getBoolean("INITIALIZED", false)
        val locality = sharedPref.getString("LOCALITY", "Location not Available")
        resultTitle.text = locality

        connectivityChecker?.apply {
            lifecycle.addObserver(this)
            connectedStatus.observe(viewLifecycleOwner, Observer<Boolean> { active ->
                if (active) {
                    Timber.i("onActivity: called")
                    noNetwork.visibility = View.GONE

                    setUpNearByData(locality!!)
                    //fetchFromLocation(6.883994, 7.421275)
                } else {
                    hideProgressBar()
                    if (!dataAvailable) {
                        noNetwork.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.homeFragment, true)
        }
    }

    private fun fetchFromLocation(lat: Double, long: Double) {
        Timber.i("fetchFromLocation Initial log")
        showProgressBar()
        val geoFire = GeoFire(dataReference)
        val queryCenter = GeoLocation(6.883994, 7.421275)
        geoQuery = geoFire.queryAtLocation(GeoLocation(6.86086, 7.41288), 3.0)
        geoQuery.addGeoQueryDataEventListener(dataListener)

        dataReference.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.i("DataReceived")
                snapshot.children.forEach {
                    val skill = it.getValue(NearBySkill::class.java)
                }
            }

        })
    }

    //fetch the user currency locality and query the cloud database
    private fun setUpNearByData(locality: String) {
        skillsReference.orderByChild("locality")
            .equalTo(locality)
            .addListenerForSingleValueEvent(nearByListener)
        //.addValueEventListener(nearByListener)
    }

    private fun setUpNearByListener() {
        nearByListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNullTo(nearBySkillList) {
                    it.getValue(RegisterUser::class.java)?.toNearBySkill()
                }.also {
                    cacheDb()
                }
            }
        }
    }

    private fun cacheDb() {
        lifecycleScope.launch {
            searchViewModel.initNearByTable(nearBySkillList)
        }
    }

    private fun setUpListener() {
        Timber.i("Listener: Initiallized")

        dataListener = object : GeoQueryDataEventListener {
            override fun onGeoQueryReady() {
                Timber.i("query is ready")
                //hideProgressBar()
            }

            override fun onDataExited(dataSnapshot: DataSnapshot?) {
                Timber.i("onDataChanged:: called")
            }

            override fun onDataChanged(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
            }

            override fun onDataEntered(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                dataSnapshot?.children!!.mapNotNullTo(_result) {
                    it.getValue(NearBySkill::class.java)
                }.also {
                    Timber.i("skills: $_result")
                    resultLiveData.postValue(_result)
                }
            }

            override fun onDataMoved(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                Timber.i("onDatamoved:: called")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Timber.i("Query error: $error")
                hideProgressBar()
            }
        }
    }

    private fun cacheDataStatue() {
        with(sharedPref.edit()) {
            putBoolean("INITIALIZED", true)
            commit()
        }
    }

    private fun setUpSearchView() {
        searchView.apply {
            val searchManager =
                requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            setIconifiedByDefault(true)
            setOnQueryTextListener(queryTextListener)
            queryHint = "Plumber,mechanics,tailor etc"

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    (activity as MainActivity).hideBottomNav()
                    titleText.visibility = View.GONE
                } else {
                    lifecycleScope.launch {
                        delay(200)
                        (activity as MainActivity).showBottomNav()
                    }
                }
            }
        }
    }

    private fun setUpQueryListener() {
        queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                (activity as MainActivity).hideKeyBoard(requireView())
                query?.let {
                    Timber.i("query:$it")
                    searchViewModel.nearByQuery(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        }
    }

    private fun showProgressBar() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        lifecycleScope.launch {
            progressBar.visibility = View.GONE
        }
    }
}
