package com.words.storageapp.client

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.client.ContractListAdapter.ClickListener
import com.words.storageapp.domain.FirebaseComment
import com.words.storageapp.domain.FirebaseContract
import com.words.storageapp.domain.FirebaseUser
import kotlinx.coroutines.launch
import timber.log.Timber

class ClientProfileFragment : Fragment() {


    private lateinit var contractDbPath: DatabaseReference
    private lateinit var clientDbPath: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: ValueEventListener
    private lateinit var progressBar: ProgressBar
    private lateinit var imageIcon: ImageView
    private lateinit var clientName: TextView
    private lateinit var clientFName: TextView
    private lateinit var contractListAdapter: ContractListAdapter
    private lateinit var contractRecyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    var client: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser!!.uid
        databaseReference = Firebase.database.reference
        contractDbPath = Firebase.database.reference.child("contracts")
        clientDbPath = Firebase.database.reference.child("clients").child(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.layout_client_profile,
            container, false
        )
        val toolbar = view.findViewById<MaterialToolbar>(R.id.clientToolBar)
        contractRecyclerView = view.findViewById(R.id.contractRecycler)
        progressBar = view.findViewById(R.id.clientProgress)
        clientName = view.findViewById(R.id.clientName)
        imageIcon = view.findViewById(R.id.clientImage)
        clientFName = view.findViewById(R.id.lName)
        val notifyBtn = view.findViewById<MaterialButton>(R.id.notify)
        val editBtn = view.findViewById<MaterialButton>(R.id.editBtn)

        fetchClient()
        setUpListener()
        contractListAdapter = ContractListAdapter(
            ClickListener(
                { contract ->
                    val action = R.id.action_clientProfileFragment_to_paymentFragment
                    val bundle = bundleOf("pay_laborer" to contract)
                    findNavController().navigate(action, bundle)

                }, { skill ->
                    ratingDialog(skill.laborerId!!)
                }, { skill ->
                    contractDbPath.child(skill.contractId!!).removeValue()
                        .addOnCompleteListener {
                            Toast.makeText(
                                requireContext(), "Item Removed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            )
        )

        notifyBtn.setOnClickListener {
            val action = R.id.action_clientProfileFragment_to_configureFragment
            findNavController().navigate(action)
        }

        editBtn.setOnClickListener {
            val action = R.id.action_clientProfileFragment_to_clientEditFragment
            client?.let {
                val bundle = bundleOf("edit_client" to client)
                findNavController().navigate(action, bundle)
            }
        }

        contractRecyclerView.adapter = contractListAdapter
        fetchContracts()

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sign_out -> {
                    firebaseAuth.signOut()
                    val action = R.id.action_clientProfileFragment_to_loginFragment
                    findNavController().navigate(action)
                    true
                }
                R.id.complaint -> {
                    val action = R.id.action_clientProfileFragment_to_complaintFragment
                    findNavController().navigate(action)
                    true
                }

                else -> false
            }
        }

        if (client != null) {
            hideProgress()
        }
        return view
    }

    override fun onStop() {
        super.onStop()
        contractDbPath.removeEventListener(listener)
    }

    private fun fetchContracts() {
        val clientId = firebaseAuth.currentUser!!.uid
        contractDbPath.orderByChild("clientId")
            .equalTo(clientId)
            .addValueEventListener(listener)

        Timber.i("clientId: $clientId")
    }

    private fun fetchClient() {
        showProgress()
        clientDbPath.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    lifecycleScope.launch {
                        snapshot.getValue(FirebaseUser::class.java)
                            ?.also { clientData ->
                                // hideProgress()
                                client = clientData
                                progressBar.visibility = View.GONE
                                clientName.text =
                                    "${clientData.firstName}" //use Resource getString here
                                clientFName.text = "${clientData.lastName}"

                                Glide.with(imageIcon.context)
                                    .load(clientData.imageUrl)
                                    .apply(
                                        RequestOptions().placeholder(R.drawable.animated_loading)
                                            .error(R.drawable.ic_icon_person)
                                    )
                                    .into(imageIcon)
                            }
                    }
                }
            }
        )
    }

    @SuppressLint("InflateParams")
    private fun ratingDialog(skillId: String) {

        val commentRef = databaseReference.child("skills")
            .child(skillId.trimIndent()).child("comments").push()

        val starCountPath = databaseReference.child("skills")
            .child(skillId.trimIndent())

        MaterialAlertDialogBuilder(requireContext()).apply {
            val inflater = LayoutInflater.from(requireContext())
            val view = inflater.inflate(R.layout.dialog_rating_layout, null)
            val commentView = view.findViewById<TextInputEditText>(R.id.commentInput)
            val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)

            setPositiveButton("Submit") { dialog, _ ->

                val rating = ratingBar.rating.toDouble()

                val commentData = FirebaseComment(
                    commentId = commentRef.key,
                    laborerId = skillId,
                    authorId = client?.id,
                    author = "${client?.firstName} ${client?.lastName}",
                    authorUrl = client?.imageUrl,
                    comment = commentView.text.toString(),
                    starNum = rating
                )

                if (commentView.text != null && client != null) {
                    commentRef.setValue(commentData).addOnCompleteListener {
                        onStartRating(starCountPath, rating)
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Cannot Submit, " +
                                "No internet", Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setView(view)
            show()
        }
    }

    private fun onStartRating(starCountPath: DatabaseReference, value: Double) {
        starCountPath.runTransaction(
            object : Transaction.Handler {
                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Rating posted successfully", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val prev = currentData.getValue(FirebaseUser::class.java)
                        ?: return Transaction.success(currentData)

                    prev.starNum = prev.starNum?.plus(value)
                    currentData.value = prev

                    Timber.i("starCounter = $currentData")
                    return Transaction.success(currentData)
                }
            }
        )
    }

    fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    private fun setUpListener() {
        listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                lifecycleScope.launch {
                    snapshot.children.mapNotNull { it.getValue(FirebaseContract::class.java) }
                        .also { contracts ->
                            hideProgress()
                            contractListAdapter.submitList(contracts)
                        }
                }
            }
        }
    }

}