package com.words.storageapp.skills

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.words.storageapp.R
import com.words.storageapp.domain.StartData
import timber.log.Timber

class SkillsPagerFragment : Fragment() {

    private var startData: StartData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            startData = it.get("SKILL_TYPE") as StartData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_skills_pager, container, false)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabs)
        val viewPager = view.findViewById<ViewPager2>(R.id.skillPager)
        val title = view.findViewById<TextView>(R.id.titleText)
        val pagerBack = view.findViewById<ImageView>(R.id.pagerBack)

        viewPager.adapter =
            SkillPagerAdapter(
                this,
                startData?.name!!,
                startData?.cats!!
            )

        title.text = startData?.name
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            startData?.cats.let { tabs ->
                tab.text = tabs?.get(position)
            }
        }.attach()

        pagerBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    //adapter class that handle the pager layouts
    class SkillPagerAdapter(
        fragment: Fragment,
        val skill: String,
        private val cats: List<String>
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = cats.size

        override fun createFragment(position: Int): Fragment =
            SkillsItemFragment.newInstance(cats[position], skill)
    }
}