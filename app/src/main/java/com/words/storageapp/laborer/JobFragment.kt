package com.words.storageapp.laborer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R


class JobFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var contractDbPath: DatabaseReference
    private lateinit var listener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        contractDbPath = Firebase.database.reference.child("contracts")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_job,
            container, false
        )
        setUpListener()

        fetchContracts()
        return view
    }

    override fun onStop() {
        super.onStop()
        contractDbPath.removeEventListener(listener)
    }

    private fun fetchContracts() {
        val labourId = firebaseAuth.currentUser!!.uid
        contractDbPath.orderByChild("laborId").equalTo(labourId)
            .addValueEventListener(listener)
    }

    private fun setUpListener() {
        listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
        }
    }

}