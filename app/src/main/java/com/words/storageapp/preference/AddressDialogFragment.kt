package com.words.storageapp.preference

import android.content.SharedPreferences
import androidx.fragment.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.words.storageapp.R
import com.words.storageapp.adapters.AddressClickListener
import com.words.storageapp.adapters.AddressListAdapter
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.Constants.ADDRESS_KEY
import java.util.ArrayList


class AddressDialogFragment : DialogFragment() {

    private val args by lazy {
        arguments?.getStringArrayList("ADDRESSES") as ArrayList<String>
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = (activity as MainActivity).sharedPref
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_address_dialog, container, false)
        val addressRecycler = view.findViewById<RecyclerView>(R.id.addressRecycle)

        val adapter = AddressListAdapter(
            args, AddressClickListener {
                storeLocationDetails(it)
                val action = R.id.action_addressDialogFragment_to_homeFragment
                findNavController().navigate(action)
            })

        addressRecycler.adapter = adapter
        return view
    }

    private fun storeLocationDetails(selectedAddress: String?) {
        with(sharedPreferences.edit()) {
            putString(ADDRESS_KEY, selectedAddress)
            commit()
        }
    }
}