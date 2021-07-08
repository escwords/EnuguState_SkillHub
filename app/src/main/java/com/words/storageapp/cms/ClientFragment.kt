package com.words.storageapp.cms

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentStatePagerAdapter
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
import com.words.storageapp.domain.FirebaseUser


class ClientFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var dataCollection: DatabaseReference
    private lateinit var clientPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database.reference
        dataCollection = database.child("clients")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client, container, false)
        clientPager = view.findViewById<ViewPager2>(R.id.clientViewPager)
        val closeBtn = view.findViewById<ImageView>(R.id.btnBack)

        closeBtn.setOnClickListener {
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
                        it.getValue(FirebaseUser::class.java)
                    }.also { clients ->
                        val adapter = ClientPagerAdapter(
                            this@ClientFragment,
                            clients
                        )
                        clientPager.adapter = adapter
                    }
                }

            }
        )
    }

    class ClientPagerAdapter(
        fa: Fragment,
        private val clients: List<FirebaseUser>
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = clients.size

        override fun createFragment(position: Int): Fragment =
            ClientActivation.newInstance(clients[position])
    }

}