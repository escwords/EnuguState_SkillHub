package com.words.storageapp.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.home.model.CreateRequest
import kotlinx.android.synthetic.main.complaint_detail.*
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestFragment : Fragment() {

    private lateinit var serviceTapCallBack:
            AdapterView.OnItemSelectedListener

    var serviceType: String? = null

    private lateinit var database: DatabaseReference
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        val spinner = view.findViewById<Spinner>(R.id.addressSpin)
        val submitBtn = view.findViewById<MaterialButton>(R.id.submitBtn)
        val phoneNumber = view.findViewById<TextView>(R.id.mobileEditText)
        val requestDesc = view.findViewById<TextView>(R.id.messageEditText)
        val fullName = view.findViewById<TextView>(R.id.fullName)
        progressBar = view.findViewById(R.id.loadingBar)

        submitBtn.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            val mobileId = phoneNumber.text.toString()
            val requestText = requestDesc.text.toString()
            val name = fullName.text.toString()

            val request = CreateRequest(
                name,
                mobileId,
                requestText,
                serviceType
            )

            phoneNumber.clearComposingText()
            requestDesc.clearComposingText()
            fullName.clearComposingText()

            database.child("requests").child(mobileId).setValue(request)
                .addOnCompleteListener {
                    Toast.makeText(
                        requireContext(),
                        "Request has been sent",
                        Toast.LENGTH_SHORT
                    ).show()

                    lifecycleScope.launch(Dispatchers.Main) {
                        loadingBar.visibility = View.GONE
                    }
                }
        }

        serviceSpinnerCallBack()
//        ArrayAdapter.createFromResource(
//            requireContext(), R.array.service_type,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            spinner.adapter = adapter
//        }

        spinner.onItemSelectedListener = serviceTapCallBack
        return view
    }

    private fun serviceSpinnerCallBack() {
        serviceTapCallBack = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val serviceString = parent?.getItemAtPosition(position) as String
                serviceType = serviceString
            }
        }
    }
}