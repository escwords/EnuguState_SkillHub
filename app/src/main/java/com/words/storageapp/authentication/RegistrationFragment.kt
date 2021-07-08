package com.words.storageapp.authentication


import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.databinding.FragmentRegistrationBinding
import com.words.storageapp.ui.main.MainActivity
import timber.log.Timber


class RegistrationFragment : Fragment() {


    private lateinit var regEmail: TextInputEditText
    private lateinit var regPassword: TextInputEditText

    var skills: String? = null
    var address: String? = null
    var account: String? = null

    private lateinit var registerFragment: RegisterFragment
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        fireDatabase = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration, container, false
        )
        regEmail = binding.emailNameText
        regPassword = binding.passwordNameText

        binding.signUp.setOnClickListener {
            (activity as MainActivity).hideKeyBoard(requireView())
                if (isValidForm()) {
                    createLabourerAccount(
                        regEmail.text.toString(),
                        regPassword.text.toString()
                    )
                }
        }
        return binding.root
    }

    private fun isValidForm(): Boolean {
        var valid = true

        val email = regEmail.text.toString()
        val password = regPassword.text.toString()

        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        val isValidPassword = password.isNotEmpty() && password.length > 7

        if (!isValidEmail) {
            regEmail.error = "Email is not valid"
            valid = false
        } else {
            regEmail.error = null
        }
        if (!isValidPassword) {
            regPassword.error = "Password is too short"
            valid = false
        } else {
            regPassword.error = null
        }
        return valid
    }


    private fun createLabourerAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("createUserWithEmail:success")
                    registerFragment.moveToNextPager()
                }
            }
    }

    private fun displayErrorDialog() {
        val alertDialog: AlertDialog? = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Error")
                setMessage(
                    "Unable to Create Account \n check your Location settings" +
                            "and Internet connection before Retry"
                )
                setPositiveButton("close") { dialog, _ ->
                    //Navigate to Profile Screen
                    dialog.dismiss()
                }
            }
            //create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }


    private fun showSnackBar(call: () -> String) {
        Snackbar.make(
            requireActivity().findViewById<ConstraintLayout>(R.id.register_layout)
            , call.invoke(), Snackbar.LENGTH_LONG
        ).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(_registerFragment: RegisterFragment) =
            RegistrationFragment().apply {
                registerFragment = _registerFragment
            }
    }
}
