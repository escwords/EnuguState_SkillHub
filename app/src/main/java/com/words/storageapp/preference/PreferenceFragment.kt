package com.words.storageapp.preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.words.storageapp.R
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.Constants.ADDRESS_KEY
import javax.inject.Inject


class PreferenceFragment : Fragment() {

    @Inject
    lateinit var viewModel: PreferenceViewModel

    private lateinit var addressSpinnerCallback:
            AdapterView.OnItemSelectedListener

    private lateinit var distanceSpinner: Spinner
    private lateinit var distanceText: TextView
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
        val close = view.findViewById<ImageView>(R.id.preferenceClose)
        distanceSpinner = view.findViewById(R.id.addressSp)
        distanceText = view.findViewById(R.id.addressTxt)
        addressCallBack()

        close.setOnClickListener {
            findNavController().navigateUp()
        }

        val address = sharedPref.getString(ADDRESS_KEY, null)
        distanceText.text = address

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.distance_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            distanceSpinner.adapter = adapter
        }
        distanceSpinner.onItemSelectedListener = addressSpinnerCallback
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
        super.onAttach(context)
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
                val distance = parent?.getItemAtPosition(position) as String
                distanceText.text = distance
                //convert to Float and Cache the distance then use it in the SkillFragment
                //cacheDistance(addressString)
            }
        }
    }

    fun cacheDistance(distance: Float) {
        with(sharedPref.edit()) {
            putFloat("Distance", distance)
            commit()
        }
    }

}