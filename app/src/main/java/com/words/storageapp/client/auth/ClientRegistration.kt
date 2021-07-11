package com.words.storageapp.client.auth

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ClientRegistration : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var clientDbPath: DatabaseReference

    private lateinit var fName: TextInputEditText
    private lateinit var lName: TextInputEditText
    private lateinit var mobile: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var passWrd: TextInputEditText
    private lateinit var compass: TextInputEditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        clientDbPath = Firebase.database.reference.child("clients")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_registration, container, false)
        val goBack = view.findViewById<ImageView>(R.id.cancelBtn)
        fName = view.findViewById(R.id.firstNameText)
        lName = view.findViewById(R.id.lastNameText)
        mobile = view.findViewById(R.id.mobileText)
        email = view.findViewById(R.id.emailNameText)
        passWrd = view.findViewById(R.id.passwordNameText)
        compass = view.findViewById(R.id.confirmNameText)
        val nextBtn: MaterialButton = view.findViewById(R.id.nextBtn)
        progressBar = view.findViewById(R.id.prgressBar)

        nextBtn.setOnClickListener {
            (activity as MainActivity).hideKeyBoard(requireView())

            if (isValidForm(email, passWrd, compass)) {
                progressBar.visibility = view.visibility

                createClientAccount(email.text.toString(), passWrd.text.toString())
            }
        }

        goBack.setOnClickListener {
            findNavController().popBackStack()
        }
        return view
    }

    private fun createClientAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("createUserWithEmail:success")
                    val userId = firebaseAuth.currentUser!!.uid

                    val firebaseUser = FirebaseUser(
                        accountStatus = "New",
                        firstName = fName.text.toString(),
                        lastName = lName.text.toString(),
                        mobile = mobile.text.toString(),
                        skillId = userId
                    )

                    clientDbPath.child(userId).setValue(firebaseUser)
                        .addOnCompleteListener {
                            Toast.makeText(
                                requireContext(),
                                "Register was successful", Toast.LENGTH_SHORT
                            ).show()
                            lifecycleScope.launch(Dispatchers.Main) {
                                progressBar.visibility = View.GONE
                                val action = R.id.action_clientRegistration_to_clientProfileFragment
                                findNavController().navigate(action)
                            }
                        }
                }
            }
    }

    private fun isValidForm(
        emailView: TextInputEditText,
        passwordView: TextInputEditText,
        confirm: TextInputEditText
    )
            : Boolean {

        var valid = true

        val email = emailView.text.toString()
        val password = passwordView.text.toString()
        val confirmPass = confirm.text.toString()

        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isValidPassword = password.isNotEmpty() && password.length > 7

        if (!isValidEmail) {
            emailView.error = "Email is not valid"
            valid = false
        } else {
            emailView.error = null
        }
        if (!isValidPassword) {
            passwordView.error = "Password is too short"
            valid = false
        } else {
            passwordView.error = null
        }

        if (password != confirmPass) {
            passwordView.error = "password does not match"
            valid = false
        } else {
            passwordView.error = null
        }
        return valid
    }

}