package com.words.storageapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import com.words.storageapp.databinding.FragmentNearByBinding
import com.words.storageapp.databinding.FragmentNoConnectionBinding

/**
 * A simple [Fragment] subclass.
 */
class NoConnection : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentNoConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

//    private fun navigateToNearByScreen() {
//
//    }

}
