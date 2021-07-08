package com.words.storageapp.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.cms.complaint.ComplaintDetail
import com.words.storageapp.cms.complaint.FirebaseComplaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text


class ComplaintFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var progressIcon: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_complaint, container, false)
        val submitBtn = view.findViewById<MaterialButton>(R.id.submitBtn)
        val phoneTextView = view.findViewById<TextView>(R.id.cMobileEditText)
        val complaintTextView = view.findViewById<TextView>(R.id.bodyEditText)
        val clientName = view.findViewById<TextView>(R.id.cNameEditText)
        progressIcon = view.findViewById(R.id.progressIcon)

        submitBtn.setOnClickListener {

            progressIcon.visibility = View.VISIBLE
            val phoneId = phoneTextView.text.toString()
            val complaintText = complaintTextView.text.toString()
            val name = clientName.text.toString()

            val complaint = FirebaseComplaint(
                author = name,
                text = complaintText,
                mobile = phoneId
            )
            phoneTextView.clearComposingText()
            complaintTextView.invalidate()
            clientName.invalidate()

            database.child("complaints").child(phoneId).setValue(complaint)
                .addOnCompleteListener {
                    Toast.makeText(
                        requireContext(),
                        "Complaint Sent Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    lifecycleScope.launch(Dispatchers.IO) {
                        progressIcon.visibility = View.VISIBLE
                    }
                }
        }
        return view
    }

}