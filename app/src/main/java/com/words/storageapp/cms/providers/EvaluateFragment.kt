package com.words.storageapp.cms.providers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.util.FORMAT

class EvaluateFragment : Fragment() {

    private var laborer: FirebaseUser? = null
    private lateinit var skillRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            laborer = it.getParcelable<FirebaseUser>("Laborer")
        }

        val database = Firebase.database.reference
        skillRef = database.child("skills").child(laborer?.id!!)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_evaluate, container, false)
        val imageUrl = view.findViewById<ImageView>(R.id.userImage)
        val firstName = view.findViewById<TextView>(R.id.firstNme)
        val lastName = view.findViewById<TextView>(R.id.lastNme)
        val skill = view.findViewById<TextView>(R.id.skillLab)
        val phoneNum = view.findViewById<TextView>(R.id.phoneNum)
        val localText = view.findViewById<TextView>(R.id.localText)
        val bankName = view.findViewById<TextView>(R.id.bankNameText)
        val accountNo = view.findViewById<TextView>(R.id.acctText)
        val time = view.findViewById<TextView>(R.id.registerDate)
        val editBtn = view.findViewById<MaterialButton>(R.id.msgBtn)
        val status = view.findViewById<TextView>(R.id.status)
        val backBtn = view.findViewById<ImageView>(R.id.backIcon)

        laborer?.also {
            firstName.text = it.firstName
            lastName.text = it.lastName
            skill.text = it.skill
            phoneNum.text = it.mobile
            localText.text = it.locality
            bankName.text = it.accountName
            accountNo.text = it.accountNumber
            status.text = it.accountStatus
            time.text = FORMAT.format(it.timeStamp).toString()

            Glide.with(imageUrl.context)
                .load(it.imageUrl)
                .apply(
                    RequestOptions().placeholder(R.drawable.animated_loading)
                        .error(R.drawable.ic_icon_person)
                )
                .into(imageUrl)

        }

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        editBtn.setOnClickListener {
            val bundle = bundleOf("Edit" to laborer)
            val action = R.id.action_admin_skill_fragment_to_profileImageFragment
            findNavController().navigate(action, bundle)
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        skillRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(FirebaseUser::class.java).also {
                    laborer = it
                }
            }
        })
    }
}