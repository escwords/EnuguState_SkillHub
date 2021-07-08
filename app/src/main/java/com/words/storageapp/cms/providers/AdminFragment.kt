package com.words.storageapp.cms.providers

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.adapters.AllSkillsAdapter
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.utilities.ItemClickListener
import timber.log.Timber
import java.util.*

class AdminFragment : Fragment() {

    private lateinit var filterSpinnerCallBack: AdapterView.OnItemSelectedListener
    private lateinit var databaseReference: DatabaseReference
    private lateinit var skillCollection: DatabaseReference

    private lateinit var skillsAdapter: AllSkillsAdapter
    private lateinit var searchView: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseReference = Firebase.database.reference
        skillCollection = databaseReference.child("skills")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_admin, container, false)
        val skillsRecycler = view.findViewById<RecyclerView>(R.id.skillsList)
        val filterSpinner = view.findViewById<Spinner>(R.id.filterSpinner)
        loadingSpinner = view.findViewById(R.id.loadingSpin)
        searchView = view.findViewById(R.id.searchBar)
        val backBtn = view.findViewById<ImageView>(R.id.bckKey)

        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        skillsAdapter = AllSkillsAdapter(ItemClickListener {
            val bundle = bundleOf("Laborer" to it)
            val action =
                R.id.action_adminFragment_to_admin_skill_fragment
            findNavController().navigate(action, bundle)
        })
        setUpQueryListener()
        setUpSearchView()
        filterSpinnerCallBack()

        skillsRecycler.adapter = skillsAdapter
        ArrayAdapter.createFromResource(
            requireContext(), R.array.filter_keys,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
        }
        filterSpinner.onItemSelectedListener = filterSpinnerCallBack
        return view
    }

    override fun onStart() {
        super.onStart()
        loadingSpinner.visibility = View.VISIBLE
        skillCollection.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull {
                    it.getValue(FirebaseUser::class.java)
                }.also { registers ->
                    skillsAdapter.submitList(registers)
                    loadingSpinner.visibility = View.GONE
                }
            }
        })
    }

    private fun filterDatabase(query: String) {
        loadingSpinner.visibility = View.VISIBLE
        skillCollection.get().addOnSuccessListener { snapsShot ->
            snapsShot.children.mapNotNull {
                it.getValue(FirebaseUser::class.java)
            }.also { skills ->
                val filtered = skills.filter { query == it.firstName || query == it.lastName }
                skillsAdapter.submitList(filtered)
                loadingSpinner.visibility = View.GONE
            }
        }.addOnFailureListener {
            Timber.e(it, "No Internet Connection")
        }
    }

    private fun setUpSearchView() {
        searchView.apply {
            //assumes that the current activity is the searchable activity
            val searchManager =
                requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
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

                Timber.i("$query")
                query?.let {
                    filterDatabase(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        }
    }

    private fun filterSpinnerCallBack() {
        filterSpinnerCallBack = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val filterString = parent?.getItemAtPosition(position) as String
                //service = serviceString
            }
        }
    }

}