package com.words.storageapp.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

import com.words.storageapp.R
import com.words.storageapp.adapters.AuthPagerAdapter
import com.words.storageapp.adapters.AuthPagerAdapter.Companion.SIGN_IN_INDEX
import com.words.storageapp.adapters.AuthPagerAdapter.Companion.SIGN_OUT_INDEX
import com.words.storageapp.databinding.FragmentAuthBinding
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.AccountType
import com.words.storageapp.util.CLIENT
import com.words.storageapp.util.LABOURER

class AuthFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore

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
        val binding = FragmentAuthBinding.inflate(inflater, container, false)
        val viewPager = binding.authPager
        val tabLayout = binding.authTabLayout

        viewPager.adapter = AuthPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        binding.closeBtn.setOnClickListener { view ->
            view.findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser?.uid
        currentUser?.let {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            SIGN_IN_INDEX -> getString(R.string.tab_sign_in)
            SIGN_OUT_INDEX -> getString(R.string.tab_sign_up)
            else -> null
        }
    }
}
