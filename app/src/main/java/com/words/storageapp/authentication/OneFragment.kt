package com.words.storageapp.authentication

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.words.storageapp.R
import com.words.storageapp.ui.main.MainActivity


class OneFragment : Fragment() {

    lateinit var registerFragment: RegisterFragment
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPref = (activity as MainActivity).sharedPref
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_one, container, false)
        val nextBtn = view.findViewById<MaterialButton>(R.id.nextBtn)
        val fName = view.findViewById<TextInputEditText>(R.id.firstNameText)
        val lName = view.findViewById<TextInputEditText>(R.id.lastNameText)
        val pNumber = view.findViewById<TextInputEditText>(R.id.mobileText)
        val locality = sharedPref.getString("LOCALITY", null)

        nextBtn.setOnClickListener {
            //move to the next pager if login was successful
            val firstName = fName.text.toString()
            val lastName = lName.text.toString()
            val mobileNumber = pNumber.text.toString()
            (activity as MainActivity).registerModel.apply {
                this.firstName = firstName
                this.lastName = lastName
                this.mobile = mobileNumber
                areaAddress = locality
            }
            registerFragment.moveToNextPager()
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(_registerFragment: RegisterFragment) =
            OneFragment().apply {
                registerFragment = _registerFragment
            }
    }
}