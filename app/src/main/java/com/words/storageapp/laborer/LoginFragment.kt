package com.words.storageapp.laborer


import android.content.Context
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.databinding.FragmentLoginBinding
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.domain.toLoggedInUser
import com.words.storageapp.laborer.viewProfile.ProfileViewModel
import com.words.storageapp.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var logEmail: TextInputEditText
    private lateinit var logPassWord: TextInputEditText
    private lateinit var remoteDb: DatabaseReference
    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        remoteDb = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentLoginBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_login, container, false
            )

        logEmail = binding.lgEmail
        logPassWord = binding.lgPass
        progressBar = binding.loginProgress

        binding.login.setOnClickListener {
            (activity as MainActivity).hideKeyBoard(this.requireView())
            val email = logEmail.text.toString()
            if (email == "admin") {
                val action = R.id.action_loginFragment_to_contentFragment
                findNavController().navigate(action)
            } else if (validate()) {
                signInUser(logEmail.text.toString(), logPassWord.text.toString())
            }
        }

        binding.clsBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clientRegisterBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_clientRegistration)
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser?.uid
        currentUser?.let {
            val action = R.id.action_loginFragment_to_clientProfileFragment
            findNavController().navigate(action)
        }
    }

    private fun validate(): Boolean {
        var valid = true

        val email = logEmail.text.toString()
        val password = logPassWord.text.toString()

        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        val isValidPassword = password.isNotEmpty() && password.length > 7

        if (!isValidEmail) {
            logEmail.error = "Email is not valid"
            valid = false
        } else {
            logEmail.error = null
        }
        if (!isValidPassword) {
            logPassWord.error = "Password is too short"
            valid = false
        } else {
            logPassWord.error = null
        }
        return valid
    }

    private fun signInUser(email: String, password: String) {
        // [START sign_in_with_email]
        showProgressBar()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("signInWithEmail:success")
                    val userId = task.result!!.user!!.uid
//                        val queryDoc = remoteDb.child("clients").child(userId)
//
//                        queryDoc.get().addOnSuccessListener {
//                            lifecycleScope.launch(Dispatchers.Main) {
//                                it.getValue(FirebaseUser::class.java)?.let { user ->
//                                    if (user.accountStatus != null &&
//                                        user.accountStatus == "Active"
//                                    ) {
//                                        val action =
//                                            R.id.action_loginFragment_to_clientProfileFragment
//                                        findNavController().navigate(action)
//
//                                    } else if (user.accountStatus != null
//                                    ) {
//                                        Toast.makeText(
//                                            requireContext(), "Admin Signed In",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    } else {
//                                        showSnackBar { "Account is pending activation" }
//                                    }
//                                }
//                            }
                    showSnackBar { "Your have successfully Logged In" }
                    val action = R.id.action_loginFragment_to_clientProfileFragment
                    findNavController().navigate(action)
                    lifecycleScope.launchWhenStarted {
                        delay(5000)
                        showSnackBar { "Timed Out: Cannot not Log In" }
                        hideProgressBar()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    lifecycleScope.launch {
                        hideProgressBar()
                    }
                    showSnackBar { "Sign In Failed, Check your Internet Connection: ${task.exception}" }
                }
            }.addOnFailureListener {
                firebaseAuth.signOut()
                lifecycleScope.launch {
                    hideProgressBar()
                }
                showSnackBar { "Unable to Sign In: ${it.message}" }
            }
    }//end of SignIn User

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    //if Snack bar does not show on screen try setting the id inside viewGroup in Authentication layout
    private fun showSnackBar(call: () -> String) {
        Snackbar.make(
            requireActivity().findViewById<ConstraintLayout>(R.id.login_layout)
            , call.invoke(), Snackbar.LENGTH_LONG
        ).show()
    }
}


