package com.words.storageapp.cms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.words.storageapp.R


class ContentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_content, container, false)
        val uploadBtn = view.findViewById<MaterialCardView>(R.id.uploadCard)
        val complaintBtn = view.findViewById<MaterialCardView>(R.id.complaint)
        val laborerCard = view.findViewById<MaterialCardView>(R.id.providersCard)
        val clientCard = view.findViewById<MaterialCardView>(R.id.clientCard)


        uploadBtn.setOnClickListener {
            val action = R.id.action_contentFragment_to_registerFragment
            findNavController().navigate(action)
        }

        complaintBtn.setOnClickListener {
            findNavController().navigate(R.id.action_contentFragment_to_resolveComplaint)
        }

        laborerCard.setOnClickListener {
            findNavController().navigate(R.id.action_contentFragment_to_adminFragment)
        }

        clientCard.setOnClickListener {

        }

        return view
    }

}