package com.words.storageapp.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.words.storageapp.laborer.LoginFragment
import com.words.storageapp.authentication.RegistrationFragment

class AuthPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    companion object {
        const val SIGN_IN_INDEX = 0
        const val SIGN_OUT_INDEX = 1
    }

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        SIGN_IN_INDEX to { LoginFragment() },
        SIGN_OUT_INDEX to { RegistrationFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}