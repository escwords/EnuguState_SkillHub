package com.words.storageapp.labourerSession

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.words.storageapp.R
import com.words.storageapp.chat.ChatsListAdapter
import com.words.storageapp.chat.ItemClickListener
import com.words.storageapp.domain.ChatData
import timber.log.Timber


class LabourerChatsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var clientChatsRef: CollectionReference
    private lateinit var chatsListener: ListenerRegistration
    private lateinit var adapter: ChatsListAdapter
    private lateinit var labourerId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Have not yet Login", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.authFragment)
        } else {
            labourerId = uid
            clientChatsRef = firestore.collection("skills")
                .document(labourerId).collection("clients")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_labourer_chats, container, false)
        val chatsRecycler = view.findViewById<RecyclerView>(R.id.labourerChatsRecycler)

        adapter = ChatsListAdapter(ItemClickListener { clientId ->
            val ids = arrayListOf<String>(labourerId, clientId)
            val bundle = bundleOf("Message" to ids)
        })
        chatsRecycler.adapter = adapter
        return view
    }


    override fun onStop() {
        super.onStop()
        attachListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        detachListener()
    }


    private fun attachListener() {
        val chats = mutableListOf<ChatData>()
        chatsListener = clientChatsRef.addSnapshotListener addSnapshotListener@{ querySnapshot, e ->
            if (e != null) {
                Timber.i(e, "Listen failed")
                return@addSnapshotListener
            }
            querySnapshot?.let {
                it.forEach { document ->
                    val chat = document.toObject(ChatData::class.java)
                    chats.add(chat)
                }
            } ?: Toast.makeText(requireContext(), "EmptyList", Toast.LENGTH_SHORT).show()
            adapter.submitList(chats)
        }
    }

    private fun detachListener() {
        chatsListener.remove()
    }
}