package com.words.storageapp.cms.complaint

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.words.storageapp.R
import com.words.storageapp.util.FORMAT
import com.words.storageapp.util.complaint_index


class ComplaintDetail : Fragment() {

    private val complaint: FirebaseComplaint? by lazy {
        arguments?.getParcelable<FirebaseComplaint>(complaint_index)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.complaint_detail, container, false)
        val fullName = view.findViewById<TextView>(R.id.fullN)
        val body = view.findViewById<TextView>(R.id.complaintBody)
        val time = view.findViewById<TextView>(R.id.timeStamp)
        val phone = view.findViewById<TextView>(R.id.mobile)

        complaint?.let { model ->
            body.text = model.text
            phone.text = model.mobile
            fullName.text = model.author
            //time.text = FORMAT.format(model.date!!)
        }
        return view
    }
}