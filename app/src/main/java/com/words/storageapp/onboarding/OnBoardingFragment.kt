package com.words.storageapp.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.words.storageapp.R
import com.words.storageapp.home.SplashTwoFragment
import com.words.storageapp.ui.main.MainActivity

class OnBoardingFragment : Fragment() {

    private lateinit var callBack: ViewPager2.OnPageChangeCallback
    private lateinit var button: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)
        val viewPager = view.findViewById<ViewPager2>(R.id.onBoardingViewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        button = view.findViewById(R.id.letContinue)
        viewPagerCallback()
        val pagerAdapter =
            OnBoardingScreenSlider(
                this
            )
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ ->
            //Some implementation
        }.attach()

        button.setOnClickListener {
            val action =
                R.id.action_onboardingFragment_to_locationFragment
            findNavController().navigate(action)
        }

        viewPager.registerOnPageChangeCallback(callBack)
        return view
    }

    override fun onStart() {
        super.onStart()
        if (!checkLocationPermissionApproved()) {
            startLocationPermission()
        }
    }

    private fun checkLocationPermissionApproved() = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun startLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            , MainActivity.REQUEST_LOCATION_CODE
        )
    }

    private fun viewPagerCallback() {
        callBack = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    button.visibility = View.GONE
                } else {
                    button.visibility = View.VISIBLE
                }
            }
        }
    }

    class OnBoardingScreenSlider(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> SplashOneFragment()
                else -> SplashTwoFragment()
            }
    }
}