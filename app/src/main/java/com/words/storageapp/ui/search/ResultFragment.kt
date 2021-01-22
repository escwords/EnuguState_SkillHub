package com.words.storageapp.ui.search
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.core.os.bundleOf
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.RecyclerView
//import com.words.storageapp.R
//import com.words.storageapp.adapters.ResultListAdapter
//import com.words.storageapp.adapters.ResultListAdapter.*
//import com.words.storageapp.databinding.FragmentResultBinding
//import com.words.storageapp.util.USERID
//
//class ResultFragment : Fragment() {
//
//    lateinit var binding: FragmentResultBinding
//    private lateinit var resultTitle: TextView
//    private lateinit var noResult: TextView
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var loadingProgress: ProgressBar
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        binding = FragmentResultBinding.inflate(inflater, container, false)
//        resultTitle = binding.resultTitle
//        noResult = binding.noResult
//        recyclerView = binding.resultRecycler
//        loadingProgress = binding.progressLoading
//
//        val viewModel = (activity as SearchResultActivity).searchViewModel
//
//        val adapter = ResultListAdapter(ClickListener { skill ->
//            val bundle =
//                bundleOf(USERID to skill.id) //call trimIndex on skillId before passing to detail or skilledFragment
//            val action = R.id.action_resultFragment_to_skilledFragment
//            findNavController().navigate(action, bundle)
//        })
//        recyclerView.adapter = adapter
//
//        viewModel.queryString.observe(viewLifecycleOwner, Observer { query ->
//            resultTitle.text = query
//            showProgressBar()
//        })
//
//        viewModel.skills.observe(viewLifecycleOwner, Observer { result ->
//            if (result.isEmpty()) {
//                //Define a loading Animated icon here
//                noResult.visibility = View.VISIBLE
//                recyclerView.visibility = View.GONE
//                hideProgressBar()
//            } else {
//                noResult.visibility = View.GONE
//                recyclerView.visibility = View.VISIBLE
//                hideProgressBar()
//                val filteredResult = result.filter { it.accountActive == true }
//                    .distinctBy { it.id }
//                adapter.submitList(filteredResult)
//            }
//        })
//        return binding.root
//    }
//
//    private fun showProgressBar() {
//        loadingProgress.visibility = View.VISIBLE
//    }
//
//    private fun hideProgressBar() {
//        loadingProgress.visibility = View.GONE
//    }
//
//}
