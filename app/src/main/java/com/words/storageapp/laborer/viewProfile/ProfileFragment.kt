package com.words.storageapp.laborer.viewProfile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.databinding.FragmentLabourerProfileBinding
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.domain.toLoggedInUser
import com.words.storageapp.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var documentRef: DatabaseReference
    private lateinit var dataListener: ValueEventListener
    private lateinit var sharePref: SharedPreferences

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    private lateinit var toolbar: MaterialToolbar
    private lateinit var warningDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        val userId = firebaseAuth.currentUser!!.uid
        sharePref = (activity as MainActivity).sharedPref
        setUpDataListener()

        fireDatabase = Firebase.database.reference
        documentRef = fireDatabase.child("skills").child(userId)
        documentRef.addValueEventListener(dataListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLabourerProfileBinding.inflate(
            inflater, container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }
        toolbar = binding.actionBar
        binding.viewModel = profileViewModel

        profileViewModel.isAcctActive.observe(viewLifecycleOwner, Observer { active ->
            active.getContentIfNotHandled()?.let {
                if (!it) {
                    noNetworkDialog()
                }
            }
        })

        binding.editProfileBtn.setOnClickListener { view ->
            view.findNavController().navigate(R.id.profileImageFragment)
        }

        //click listener that takes you to work album
        binding.worksBtn.setOnClickListener { view ->
            view.findNavController().navigate(R.id.editAlbumFragment)
        }

        binding.settingBtn.setOnClickListener { view ->
            view.findNavController().navigate(R.id.configureFragment)
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sign_out -> {
                    firebaseAuth.signOut()
                    true
                }
                else -> false
            }
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    override fun onStop() {
        super.onStop()
        documentRef.removeEventListener(dataListener)
    }

    private fun setUpDataListener() {
        dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updateDb = sharePref.getBoolean(
                    "UPDATE",
                    false
                ) //variable boolean value to tell when data need update

                val data = snapshot.getValue(FirebaseUser::class.java) //as RegisterUser
                lifecycleScope.launch(Dispatchers.IO) {
                    if (!updateDb) {
                        profileViewModel.initLaborerAcct(data?.toLoggedInUser())
                        cachedFirstData()
                    } else {
                        profileViewModel.updateAccount(data?.toLoggedInUser())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun cachedFirstData() {
        with(sharePref.edit()) {
            putBoolean("UPDATE", true)
            commit()
        }
    }

    private fun noNetworkDialog() {
        warningDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it).apply {
                setTitle("Notice!!")
                setMessage("Complete Profile details to Activate Account.")
                setPositiveButton("close") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            builder.create()
        }
        warningDialog.show()
    }

}