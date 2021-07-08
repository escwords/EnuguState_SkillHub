package com.words.storageapp.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.words.storageapp.R

class NotificationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_configure, container, false)
        val bck = view.findViewById<ImageView>(R.id.settingClose)
        bck.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

}
