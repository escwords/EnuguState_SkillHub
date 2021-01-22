package com.words.storageapp.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.words.storageapp.R
import com.words.storageapp.domain.ChatData
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.AccountType
import com.words.storageapp.util.CLIENT
import timber.log.Timber

class MessagesFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var messagesRef: CollectionReference
    private lateinit var messagesListener: ListenerRegistration
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var editText: TextInputEditText
    private lateinit var sendBtn: ImageView

    private val args by lazy {
        arguments?.getStringArrayList("Message")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        args?.let { ids ->
            messagesRef = firestore.collection("skills").document(ids[0])
                .collection("clients").document(ids[1]).collection("messages")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        messagesRecycler = view.findViewById<RecyclerView>(R.id.messagesRecycler)
        sendBtn = view.findViewById<ImageView>(R.id.sendBtn)
        editText = view.findViewById<TextInputEditText>(R.id.textInputEditText)
        val currentId = firebaseAuth.currentUser?.uid

        sendBtn.setOnClickListener {
            currentId?.let { uid ->
                val text = editText.text.toString()
                val documentRef = messagesRef.document()
                val messageData = MessageData(
                    messageId = documentRef.id,
                    senderId = uid,
                    messageText = text
                )
                Toast.makeText(
                    requireContext(), "Sending Message...",
                    Toast.LENGTH_SHORT
                ).show()

                documentRef.set(messageData).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Timber.i(e, "Failed to Send Message")
                    Toast.makeText(
                        requireContext(), "Message Failed, Check your Internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            editText.setText("")
        }
        return view
    }

    fun isChatSaved() {
        val sharedPref = (activity as MainActivity).sharedPref
        with(sharedPref.edit()) {
            putBoolean("ChatSaved", true)
            commit()
        }
    }

    fun cacheUser() {
        val sharedPref = (activity as MainActivity).sharedPref
        val accountType = sharedPref.getString(AccountType, null)
        val isChatSaved = sharedPref.getBoolean("ChatSaved", false)
        if (accountType == CLIENT && !isChatSaved) {

        }
    }

    fun editTextViewListener() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendBtn.isEnabled = s.toString().trim().isNotEmpty()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        attachChatListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        detachListener()
    }

    private fun attachChatListener() {
        val messages = mutableListOf<MessageData>()
        messagesListener = messagesRef.addSnapshotListener addSnapshotListener@{ querySnapshot, e ->
            if (e != null) {
                Timber.i(e, "Listen failed")
                return@addSnapshotListener
            }
            querySnapshot?.let {
                it.forEach { document ->
                    val message = document.toObject(MessageData::class.java)
                    messages.add(message)
                }
            } ?: Toast.makeText(requireContext(), "EmptyList", Toast.LENGTH_SHORT).show()

            args?.let { ids ->
                messageListAdapter = MessageListAdapter(messages, ids[0])
                messagesRecycler.adapter = messageListAdapter
            }
        }
    }

    private fun detachListener() {
        messagesListener.remove()
    }

}