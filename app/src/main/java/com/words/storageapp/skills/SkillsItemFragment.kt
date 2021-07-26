package com.words.storageapp.skills

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.adapters.ResultListAdapter
import com.words.storageapp.domain.StartData
import com.words.storageapp.preference.PreferenceViewModel
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.ui.search.SearchViewModel
import com.words.storageapp.util.USERID
import com.words.storageapp.util.utilities.getDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val SKILL = "skill_title"

class SkillsItemFragment : Fragment() {

    private var title: String? = null
    private var category: String? = null

    var latitude: Double? = null
    var longitude: Double? = null
    private lateinit var geoLocation: GeoLocation
    private lateinit var noResultLayout: ConstraintLayout
    private lateinit var loadingBar: ProgressBar
    private lateinit var searchBar: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var skillList: RecyclerView
    private lateinit var adapter: ResultListAdapter

    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var viewModel: SearchViewModel //here we will inject the searchViewModel

    @Inject
    lateinit var prefViewModel: PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(SKILL)
            category = it.getString("Skill_Cat")
        }
        viewModel.queryDb(title!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_skill_item,
            container, false
        )
        skillList = view.findViewById(R.id.skillList)
        noResultLayout = view.findViewById(R.id.noResult)
        loadingBar = view.findViewById(R.id.nearLoading)
        searchBar = view.findViewById(R.id.searchBar)
        sharedPref = (activity as MainActivity).sharedPref

        setUpQueryListener()
        setUpSearchView()

        val location = sharedPref.getBoolean("LOCATION_REQUESTED", false)

        prefViewModel.addresses.observe(viewLifecycleOwner, Observer { address ->
            address?.let {
                if (!location) {
                    defaultGeoLocation()
                } else {
                    val addressModel = it[0]
                    geoLocation = GeoLocation(addressModel.latitude!!, addressModel.longitude!!)
                }
            }
        })

        adapter = ResultListAdapter(ResultListAdapter.ClickListener {
            val bundle =
                bundleOf(USERID to it.id) //call trimIndex on skillId before passing to detail or skilledFragment
            val action =
                R.id.action_skillsPagerFragment_to_skilledFragment2
            findNavController().navigate(action, bundle)
        })
        skillList.adapter = adapter
        return view
    }

    //default geoLocation to use to avoid unInitialized geoLocation
    private fun defaultGeoLocation() {
        geoLocation = GeoLocation(6.8719678, 7.4008198)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        loadingBar(true)
        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000)
            getNearByLaborer()
        }
    }

    //this code computes the nearest skilled laborer considering distance
    private fun getNearByLaborer() {
        viewModel.skills.observe(viewLifecycleOwner, Observer {  //replace allSkills with skills
            if (it.isEmpty()) {
                loadingBar(false)
                Timber.i("Result: $it")
                showEmptyResult(true)
            } else {

                it.asSequence().filter { miniData ->
                    getDistance(
                        geoLocation,
                        miniData.latitude!!,
                        miniData.longitude!!
                    ) <= 600.0
                }.filter { d ->
                    d.skill == title &&
                            d.serviceOffered!!.substringBefore(" ").contains(category!!)
                            || d.serviceOffered!!.contains(category!!)
                }.toList().also { nearBy ->
                    showEmptyResult(false)
                    loadingBar(false)
                    Timber.i("result: $nearBy")

                    if (nearBy.isNullOrEmpty()) {
                        showEmptyResult(true)
                    }
                    adapter.submitList(nearBy)
                }
            }
        })
    }

    private fun showEmptyResult(show: Boolean) {
        if (show) {
            noResultLayout.visibility = View.VISIBLE
            skillList.visibility = View.GONE
        } else {
            noResultLayout.visibility = View.GONE
            skillList.visibility = View.VISIBLE
        }
    }

    private fun loadingBar(show: Boolean) {
        if (show) {
            loadingBar.visibility = View.VISIBLE
        } else {
            loadingBar.visibility = View.GONE
        }
    }

    //set up the search Bar
    private fun setUpSearchView() {
        searchBar.apply {
            //assumes that the current activity is the searchable activity
            val searchManager =
                requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            setIconifiedByDefault(false)
            isSubmitButtonEnabled = true
            setOnQueryTextFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    (activity as MainActivity).showKeyBoard(v)
                }
            }
            setOnQueryTextListener(queryTextListener)
        }
    }

    private fun setUpQueryListener() {
        queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                (activity as MainActivity).hideKeyBoard(requireView())
                query?.let {
                    viewModel.queryDb(query)
                    loadingBar(true)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SkillsItemFragment().apply {
                arguments = Bundle().apply {
                    putString("Skill_Cat", param1)
                    putString(SKILL, param2)
                }
            }
    }

}