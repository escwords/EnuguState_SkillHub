package com.words.storageapp.skills

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoLocation
import com.words.storageapp.R
import com.words.storageapp.adapters.ResultListAdapter
import com.words.storageapp.adapters.ResultListAdapter.*
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


class SkillFragment : Fragment() {


    var latitude: Double? = null
    var longitude: Double? = null
    private lateinit var geoLocation: GeoLocation
    private lateinit var noResultLayout: ConstraintLayout
    private lateinit var loadingBar: ProgressBar

    private lateinit var skillList: RecyclerView
    private lateinit var adapter: ResultListAdapter

    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var viewModel: SearchViewModel //here we will inject the searchViewModel

    @Inject
    lateinit var prefViewModel: PreferenceViewModel

    private val args: String by lazy {
        arguments?.get("SKILL_TYPE") as String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.queryDb(args)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_skill, container, false)
        skillList = view.findViewById(R.id.skillList)
        noResultLayout = view.findViewById(R.id.noResult)
        loadingBar = view.findViewById(R.id.nearLoading)
        sharedPref = (activity as MainActivity).sharedPref
        val title = view.findViewById<TextView>(R.id.skillName)
        val closeBtn = view.findViewById<ImageView>(R.id.closeBtn)
        title.text = args

        closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
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

        adapter = ResultListAdapter(ClickListener {
            val bundle =
                bundleOf(USERID to it.id) //call trimIndex on skillId before passing to detail or skilledFragment
            val action =
                R.id.action_skillFragment_to_skilledFragment2
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

    private fun getNearByLaborer() {
        viewModel.skills.observe(viewLifecycleOwner, Observer {  //replace allSkills with skills
            if (it.isEmpty()) {
                loadingBar(false)
                showEmptyResult(true)
            } else {
                Timber.i("result: $it")

                it.asSequence().filter { miniData ->
                    getDistance(
                        geoLocation,
                        miniData.latitude!!,
                        miniData.longitude!!
                    ) <= 600.0
                }.toList().also { nearBy ->
                    showEmptyResult(false)
                    loadingBar(false)

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

}