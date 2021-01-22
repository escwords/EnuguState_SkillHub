package com.words.storageapp.ui.account


import android.content.Context
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.words.storageapp.R
import com.words.storageapp.database.model.toClientDbModel
import com.words.storageapp.databinding.FragmentLoginBinding
import com.words.storageapp.domain.RegisterUser
import com.words.storageapp.domain.toLoggedInUser
import com.words.storageapp.ui.account.viewProfile.ProfileViewModel
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.AccountType
import com.words.storageapp.util.CLIENT
import com.words.storageapp.util.LABOURER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginFragment : Fragment(), View.OnClickListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var logEmail: TextInputEditText
    private lateinit var logPassWord: TextInputEditText
    private lateinit var fireStore: FirebaseFirestore

    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
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

        binding.login.setOnClickListener(this)

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.login -> {
                if (validate()) {
                    (activity as MainActivity).hideKeyBoard(this.requireView())
                    signInUser(logEmail.text.toString(), logPassWord.text.toString())
                }
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        // [START sign_in_with_email]
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("signInWithEmail:success")
                    val user = task.result!!.user
                    initializeProfile(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e("signInWithEmail:failure ${task.exception}")
                    progressBar.visibility = View.GONE
                    showSnackBar { "Sign In Failed, Check your Internet Connection: ${task.exception}" }
                }
                // [END_EXCLUDE]
            }
    }//end of SignIn User

    //if Snack bar does not show on screen try setting the id inside viewGroup in Authentication layout
    private fun showSnackBar(call: () -> String) {
        Snackbar.make(
            requireActivity().findViewById<ConstraintLayout>(R.id.login_layout)
            , call.invoke(), Snackbar.LENGTH_LONG
        ).show()
    }

    private fun initializeProfile(firebaseUser: FirebaseUser?) {
        val currentUserId = firebaseUser?.uid
        if (currentUserId != null) {
            val documentRef =
                fireStore.collection(getString(R.string.fireStore_node)).document(currentUserId)

            documentRef.get().addOnSuccessListener { documentSnapshot ->
                lifecycleScope.launch(Dispatchers.Main) {

                    val user = documentSnapshot.toObject<RegisterUser>()
                    user?.let { registeredUser ->

                        profileViewModel.initializeAccount(
                            registeredUser.toLoggedInUser().also {
                                Timber.i("LoggedUser: $it")
                            })
                        progressBar.visibility = View.GONE
                        val action = R.id.action_authFragment_to_profileFragment
                        findNavController().navigate(action)
                    }
                }

            }.addOnFailureListener { e ->
                Timber.e(e, "Failed to Retrieve User")
                //show no internet connection
                firebaseAuth.signOut()
                showSnackBar { "Error: Try Signing in again" }
            }
        } else {
            Timber.i("UserId is Null")
        }
    }

    private fun cacheAccountType(account: String) {
        val sharedPref = (activity as MainActivity).sharedPref
        with(sharedPref.edit()) {
            putString(AccountType, account)
            commit()
        }
    }
}

