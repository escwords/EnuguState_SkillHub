package com.words.storageapp.cms.complaint

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.util.complaint_index


class ResolveComplaint : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var dataCollection: DatabaseReference
    private lateinit var complaintViewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference
        dataCollection = database.child("complaints")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.resolve_complaint, container, false)
        complaintViewPager = view.findViewById<ViewPager2>(R.id.complaintViewPager)
        val btnClose = view.findViewById<ImageView>(R.id.btnBack)

        btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dataCollection.addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.mapNotNull {
                        it.getValue(FirebaseComplaint::class.java)
                    }.also { complaint ->
                        val adapter = ComplaintPagerAdapter(
                            this@ResolveComplaint,
                            complaint
                        )
                        complaintViewPager.adapter = adapter
                    }
                }
            }
        )
    }

    class ComplaintPagerAdapter(
        fa: Fragment, private val complaints: List<FirebaseComplaint>
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = complaints.size

        override fun createFragment(position: Int): Fragment = ComplaintDetail().apply {
            val bundle = bundleOf(complaint_index to complaints[position])
            arguments = bundle
        }
    }

}