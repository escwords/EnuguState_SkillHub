package com.words.storageapp.ui.search

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
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
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    private var searchJob: Job? = null
    private lateinit var skillListRecycler: RecyclerView
    lateinit var adapter: ResultListAdapter
    private lateinit var searchView: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener

    private val sharedPref: SharedPreferences by lazy {
        requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    @Inject
    lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val query = sharedPref.getString(getString(R.string.query_text), "")

        searchView = binding.searchBar
        skillListRecycler = binding.skillList

        sharedPref.getString(getString(R.string.query_text), null).let {
            if (!it.isNullOrBlank()) {
                binding.resultTitle.text = query
                viewModel.queryDb(it)
            }
        }

        setUpQueryListener()
        setUpSearchView()

        binding.backBtn.setOnClickListener {
            (activity as MainActivity).hideKeyBoard(requireView())
            findNavController().navigateUp()
        }

        adapter = ResultListAdapter(ClickListener { skill ->
            val bundle =
                bundleOf(USERID to skill.id) //call trimIndex on skillId before passing to detail or skilledFragment
            val action = R.id.action_searchFragment_to_skilledFragment2
            findNavController().navigate(action, bundle)
        })
        skillListRecycler.adapter = adapter

        viewModel.skills.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                showEmptyResult(true)
            } else {
                showEmptyResult(false)
                adapter.submitList(it)
            }
        })

        viewModel.allSkills.observe(viewLifecycleOwner, Observer {
//            Toast.makeText(requireContext(),
//                "skills : $it",Toast.LENGTH_SHORT).show()
        })

        return binding.root
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
            queryHint = context.getString(R.string.searchQuery)
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

                binding.resultTitle.text = query
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

    private fun showInputMethod(view: View) {
        val imm: InputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }


}