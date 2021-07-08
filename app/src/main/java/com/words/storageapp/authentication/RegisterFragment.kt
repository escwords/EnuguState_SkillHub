package com.words.storageapp.authentication

import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.words.storageapp.R


class RegisterFragment : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var callback: ViewPager2.OnPageChangeCallback
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var backPressedCallBack: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        viewPager2 = view.findViewById<ViewPager2>(R.id.registerAuthPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        val cancel = view.findViewById<ImageView>(R.id.goBack)

        val adapter = RegisterPager(this, this)
        viewPager2.isUserInputEnabled = false //dis-activate viewpager swiping

        initPagerCallback()
        setBackPressedCallBack()
        viewPager2.adapter = adapter

        cancel.setOnClickListener {
            findNavController().popBackStack()
        }

        TabLayoutMediator(tabLayout, viewPager2) { _, _ ->
            //Some implementation
        }.attach()

        viewPager2.registerOnPageChangeCallback(callback)
        activity?.onBackPressedDispatcher?.addCallback(backPressedCallBack)

        return view
    }

    private fun setBackPressedCallBack() {
        backPressedCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
    }

    fun moveToNextPager() {
        viewPager2.currentItem = viewPager2.currentItem + 1 //increments the pager.
    }

    fun moveToPrevPager() {
        viewPager2.currentItem = viewPager2.currentItem - 1 //decrements the pager
    }

    fun navigateToProcess() {
        findNavController().navigate(R.id.action_registerFragment_to_processFragment)
    }

    private fun initPagerCallback() {
        callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }
        }
    }

    class RegisterPager(
        val fragment: Fragment,
        private val registerFragment: RegisterFragment
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount() = 4
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> OneFragment.newInstance(registerFragment)
                1 -> TwoFragment.newInstance(registerFragment)
                2 -> ThreeFragment.newInstance(registerFragment)
                else -> FourFragment.newInstance(registerFragment)
            }
        }
    }
}