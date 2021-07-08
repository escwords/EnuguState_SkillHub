package com.words.storageapp.ui.search

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.words.storageapp.R
import com.words.storageapp.adapters.ResultListAdapter
import com.words.storageapp.adapters.ResultListAdapter.*
import com.words.storageapp.databinding.FragmentSearchBinding
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.USERID
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.words.storageapp.home.StartFragment
import com.words.storageapp.preference.PreferenceViewModel
import com.words.storageapp.ui.main.StartViewModel
import com.words.storageapp.util.utilities.ItemClickListener
import com.words.storageapp.util.utilities.getDistance
import kotlinx.android.synthetic.main.fragment_status_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    private lateinit var skillListRecycler: RecyclerView
    lateinit var adapter: ResultListAdapter
    private lateinit var searchView: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var recentRecycler: RecyclerView
    private lateinit var recycleLayout: ConstraintLayout
    //private lateinit var nearbyLoading: ProgressBar


    private val sharedPref: SharedPreferences by lazy {
        requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    @Inject
    lateinit var viewModel: SearchViewModel

    @Inject
    lateinit var startViewModel: StartViewModel

    @Inject
    lateinit var prefViewModel: PreferenceViewModel //implemented for the location

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        recycleLayout = binding.constraintLayout4
        //nearbyLoading = binding.nearByLoading
        searchView = binding.searchBar
        skillListRecycler = binding.skillList
        recentRecycler = binding.recentRecycler

        sharedPref.getString(getString(R.string.query_text), null).let {
            if (!it.isNullOrBlank()) {
                binding.searchBar.queryHint = it
                viewModel.queryDb(it)
            }
        }

        setUpQueryListener()
        setUpSearchView()

        binding.backBtn.setOnClickListener {
            (activity as MainActivity).hideKeyBoard(requireView())
            findNavController().navigateUp()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            startViewModel.recentSkill.observe(viewLifecycleOwner, Observer { recentData ->
                if (recentData.isNullOrEmpty()) {
                    recycleLayout.visibility = View.GONE
                } else {
                    recycleLayout.visibility = View.VISIBLE
                    val recentAdapter =
                        StartFragment.RecentRecyclerAdapter(
                            recentData,
                            ItemClickListener { data ->
                                val bundle = bundleOf(USERID to data.id)
                                val action =
                                    R.id.action_searchFragment_to_skilledFragment2
                                findNavController().navigate(action, bundle)
                            })
                    recentRecycler.adapter = recentAdapter
                }
            })
        }

        adapter = ResultListAdapter(ClickListener { skill ->
            val bundle =
                bundleOf(USERID to skill.id) //call trimIndex on skillId before passing to detail or skilledFragment
            val action = R.id.action_searchFragment_to_skilledFragment2
            findNavController().navigate(action, bundle)
        })
        skillListRecycler.adapter = adapter
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        viewModel.skills.observe(viewLifecycleOwner, Observer { it ->
            if (it.isEmpty()) {
                showEmptyResult(true)
                // nearbyLoading.visibility = View.GONE
            } else {
                //nearbyLoading.visibility = View.GONE
                showEmptyResult(false)
                adapter.submitList(it)
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    private fun setUpSearchView() {
        searchView.apply {
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

    private fun showEmptyResult(show: Boolean) {
        if (show) {
            binding.noResult.visibility = View.VISIBLE
            binding.skillList.visibility = View.GONE
        } else {
            binding.noResult.visibility = View.GONE
            binding.skillList.visibility = View.VISIBLE
        }
    }

    fun savedQuery(query: String?) {
        with(sharedPref.edit()) {
            putString(getString(R.string.query_text), query)
            commit()
        }
    }

    private fun setUpQueryListener() {
        queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                (activity as MainActivity).hideKeyBoard(requireView())
                savedQuery(query)

                query?.let {
                    viewModel.queryDb(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        }
    }

}