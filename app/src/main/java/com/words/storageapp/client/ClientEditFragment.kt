package com.words.storageapp.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.words.storageapp.R
import com.words.storageapp.domain.FirebaseUser

class ClientEditFragment : Fragment() {

    var client: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            client = it.getParcelable<FirebaseUser>("edit_client") as FirebaseUser
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_edit, container, false)
        val saveBtn = view.findViewById<MaterialButton>(R.id.updateBtn)
        val profileImage = view.findViewById<ImageView>(R.id.profilePics_edit)
        val firstName = view.findViewById<TextInputEditText>(R.id.firstNameText)
        val lastName = view.findViewById<TextInputEditText>(R.id.lastNameText)
        val backKey = view.findViewById<ImageView>(R.id.backKey)

        Glide.with(profileImage.context)
            .load(client?.imageUrl)
            .into(profileImage)

        firstName.hint = client?.firstName
        lastName.hint = client?.lastName

        saveBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        backKey.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

}