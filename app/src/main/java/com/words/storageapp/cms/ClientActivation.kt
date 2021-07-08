package com.words.storageapp.cms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.words.storageapp.R
import com.words.storageapp.domain.FirebaseUser


private const val CLIENT = "client"

class ClientActivation : Fragment() {

    private var client: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            client = it.getParcelable(CLIENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_activation, container, false)

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(data: FirebaseUser) =
            ClientActivation().apply {
                arguments = Bundle().apply {
                    putParcelable(CLIENT, client)
                }
            }
    }
}