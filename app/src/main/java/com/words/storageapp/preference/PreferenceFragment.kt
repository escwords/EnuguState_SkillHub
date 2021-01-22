package com.words.storageapp.preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.Observer
import com.words.storageapp.R
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.Constants.ADDRESS_KEY
import javax.inject.Inject


class PreferenceFragment : Fragment() {

    @Inject
    lateinit var viewModel: PreferenceViewModel

    private lateinit var addressSpinnerCallback:
            AdapterView.OnItemSelectedListener

    private lateinit var localitySpinnerCallback:
            AdapterView.OnItemSelectedListener

    private lateinit var addressSpinner: Spinner
    private lateinit var localitySpinner: Spinner
    private lateinit var addressText: TextView
    private lateinit var localityText: TextView


    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = (activity as MainActivity).sharedPref
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_preference, container, false)
        addressSpinner = view.findViewById(R.id.addressSp)
        //localitySpinner = view.findViewById(R.id.localitySp)
        addressText = view.findViewById(R.id.addressTxt)
        localityText = view.findViewById(R.id.localityTxt)
        addressCallBack()
        //localityCallBack()

        val address = sharedPref.getString(ADDRESS_KEY, null)
        val locality = sharedPref.getString("LOCALITY", null)

        addressText.text = address
        localityText.text = locality

        viewModel.addressString.observe(viewLifecycleOwner, Observer {
            setUpAddressAdapter(it)
        })

        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
        super.onAttach(context)
    }

    private fun setUpAddressAdapter(addresses: List<String?>) {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addresses
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addressSpinner.adapter = adapter
        }
    }

    private fun setUpLocationAdapter(addresses: List<String?>) {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addresses
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            localitySpinner.adapter = adapter
        }
    }

    private fun localityCallBack() {
        localitySpinnerCallback = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val localityString = parent?.getItemAtPosition(position) as String
                localityText.text = localityString
                cacheLocality(localityString)
            }
        }
    }

    private fun addressCallBack() {
        addressSpinnerCallback = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val addressString = parent?.getItemAtPosition(position) as String
                addressText.text = addressString
                cacheAddress(addressString)
            }
        }
    }

    fun cacheAddress(address: String) {
        with(sharedPref.edit()) {
            putString(ADDRESS_KEY, address)
            commit()
        }
    }

    fun cacheLocality(locality: String) {
        with(sharedPref.edit()) {
            putString("LOCALITY", locality)
            commit()
        }
    }

}