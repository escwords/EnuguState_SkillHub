package com.words.storageapp.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.words.storageapp.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAboutBinding.inflate(inflater, container, false)
        val title = binding.bTitle
        val body = binding.chargeText

        val content = listOf(
            """
            A Project Submitted to the Department of Computer Science In Partial fulfilment of the requirement for the award of Bsc.(B.A Hons) Degree in Computer Science
        """.trimIndent(),
            """
                Credit: 
                
                Designer: Nnamani Emmanuel 
                
                SuperVisor: Dr. Honour Nwagwu
            """.trimIndent()
        )
        title.text = content[0]
        body.text = content[1]
        return binding.root
    }

}