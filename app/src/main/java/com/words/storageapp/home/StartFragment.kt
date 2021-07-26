package com.words.storageapp.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.words.storageapp.R
import com.words.storageapp.database.model.RecentSkillModel
import com.words.storageapp.domain.StartData
import com.words.storageapp.preference.PreferenceViewModel
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.*
import com.words.storageapp.util.utilities.ItemClickListener
import com.words.storageapp.util.utilities.getJsonFromAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


class StartFragment : Fragment() {


    private lateinit var startRecycler: RecyclerView
    private lateinit var sharedPref: SharedPreferences
    private var showOnBoarding: Boolean = false
    lateinit var callback: OnBackPressedCallback

    @Inject
    lateinit var prefViewModel: PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = (activity as MainActivity).sharedPref
        showOnBoarding = sharedPref.getBoolean("OnBoarding", false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        startRecycler = view.findViewById(R.id.startRecycler)
        val settingBtn = view.findViewById<ImageView>(R.id.option)

        setUpOnBackPressedCallback()

        settingBtn.setOnClickListener {
            val action = R.id.action_startFragment_to_preferenceFragment
            findNavController().navigate(action)
        }


        if (!showOnBoarding) {
            val action = R.id.action_startFragment_to_onboardingFragment
            findNavController().navigate(action)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        //ask user if they want to exit the app
        return view
    }

    private fun setUpOnBackPressedCallback() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Do you want to Exit?")
            setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                (activity as MainActivity).finish()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            val startData = getJsonFromAsset(requireContext(), START_SKILL)
            Timber.i("startData :$startData")

            withContext(Dispatchers.Main) {
                val startAdapter =
                    StartListAdapter(
                        startData,
                        ItemClickListener { data ->
                            val bundle = bundleOf("SKILL_TYPE" to data)
                            val action =
                                R.id.action_startFragment_to_skillsPagerFragment
                            findNavController().navigate(action, bundle)
                        })
                startRecycler.adapter = startAdapter
            }
        }

    }

    /*this class is the recyclerview adapter for the SkillType list RecyclerView*/
    class StartListAdapter(
        private val startData: List<StartData>,
        val listener: ItemClickListener<StartData>
    ) :
        RecyclerView.Adapter<StartListAdapter.StartListViewModel>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StartListViewModel {
            val inflater = LayoutInflater.from(parent.context)
            return StartListViewModel(inflater.inflate(R.layout.item_start_layout, parent, false))
        }

        override fun getItemCount(): Int = startData.size

        override fun onBindViewHolder(holder: StartListViewModel, position: Int) {
            val startData = startData[position]
            holder.bind(startData, listener)
        }

        inner class StartListViewModel(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

            private val iconImage: ImageView = itemView.findViewById(R.id.recentIcon)
            private val iconTitle: TextView = itemView.findViewById(R.id.lname)

            private val resource = itemView.resources

            @SuppressLint("Recycle")
            fun bind(startData: StartData, listener: ItemClickListener<StartData>) {
                iconTitle.text = startData.name
                itemView.setOnClickListener {
                    listener.clickAction(startData)
                }
                with(iconImage) {
                    val cover = resource.obtainTypedArray(R.array.skill_Cat_drawables)
                    setImageResource(cover.getResourceId(startData.index, 0))
                }
            }
        }

    }

    /*This class is the recyclerview adapter for the Recent list adapter*/
    class RecentRecyclerAdapter(
        private val recentData: List<RecentSkillModel>,
        val clickListener: ItemClickListener<RecentSkillModel>
    ) :
        RecyclerView.Adapter<RecentRecyclerAdapter.RecentViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return RecentViewHolder(inflater.inflate(R.layout.item_recent_layout, parent, false))
        }

        override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
            val recentSkill = recentData[position]
            holder.bind(recentSkill, clickListener)
        }

        override fun getItemCount(): Int = recentData.size

        inner class RecentViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

            private val iconImage: ImageView = itemView.findViewById(R.id.recentIcon)
            private val skill: TextView = itemView.findViewById(R.id.skillType)

            fun bind(
                recentData: RecentSkillModel,
                listener: ItemClickListener<RecentSkillModel>
            ) {
                skill.text = recentData.skill

                itemView.setOnClickListener {
                    recentData.let {
                        listener.onClick(it)
                    }
                }

                Glide.with(iconImage.context)
                    .load(recentData.imgUrl)
                    .apply(RequestOptions().placeholder(R.drawable.default_profile_icon))
                    .into(iconImage)
            }
        }
    }
}