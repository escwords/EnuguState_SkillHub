package com.words.storageapp.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.words.storageapp.R
import com.words.storageapp.adapters.PhotosAdapter
import com.words.storageapp.database.model.toNearBySkill
import com.words.storageapp.databinding.FragmentDetail2Binding
import com.words.storageapp.domain.Photo
import com.words.storageapp.domain.RegisterUser
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.ui.search.SearchRepository
import com.words.storageapp.util.InjectorUtil
import com.words.storageapp.util.USERID
import timber.log.Timber
import javax.inject.Inject

class SkilledFragment : Fragment() {

    private val skillId: String by lazy {
        arguments?.get(USERID) as String
    }

    @Inject
    lateinit var searchRepository: SearchRepository

    private val detailViewModel: DetailViewModel by viewModels {
        InjectorUtil.provideDetailViewModelFactory(skillId, searchRepository)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var photoCollectionRef: DatabaseReference
    private lateinit var collectionRef: CollectionReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotosAdapter
    private lateinit var fireStore: FirebaseFirestore

    //private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var registration: ListenerRegistration
    private lateinit var messageBtn: MaterialButton
    private lateinit var callBtn: MaterialButton
    private lateinit var photoListener: ValueEventListener
    val photoList = mutableListOf<Photo>() //list that contain all pictures of works

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireStore = FirebaseFirestore.getInstance()
        ///firebaseAuth = FirebaseAuth.getInstance()
        collectionRef =
            fireStore.collection(getString(R.string.db_node)).document(skillId.trimIndent())
                .collection("photos")

        databaseReference = Firebase.database.reference
        photoCollectionRef = databaseReference.child("skills")
            .child(skillId.trimIndent()).child("photos")

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDetail2Binding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.detailModel = detailViewModel
        callBtn = binding.callBtn
        messageBtn = binding.messageBtn
        recyclerView = binding.photoRecycler
        adapter = PhotosAdapter()
        //attachPhotoListener()
        setPhotoListener()
        recyclerView.adapter = adapter


        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        Toast.makeText(
            requireContext(), "skillId:$skillId",
            Toast.LENGTH_SHORT
        ).show()

        callBtn.setOnClickListener {

            detailViewModel.mobile.observe(viewLifecycleOwner, Observer { phoneNo ->
                if (phoneNo != null) {
                    Timber.i("mobile: $phoneNo")
                    dialPhoneNumber(phoneNo)
                }
            })
        }

        messageBtn.setOnClickListener {
            detailViewModel.mobile.observe(viewLifecycleOwner, Observer { phoneNo ->
                if (phoneNo != null) {
                    Timber.i("mobile: $phoneNo")
                    composeSmsMessage(phoneNo)
                }
            })
        }

        detailViewModel.detailData.observe(viewLifecycleOwner, Observer { data ->
            Timber.i("Photos observer is called")
            Timber.i("PhotoList: $data")
            if (data == null) {
                Timber.i("userData is null")
            } else {
                Timber.i("userData $data")
            }
        })
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        photoCollectionRef.addValueEventListener(photoListener)
    }

    override fun onStop() {
        super.onStop()
        // detachListener()
        photoCollectionRef.removeEventListener(photoListener)
    }

//    private fun attachPhotoListener() {
//        val list = mutableListOf<Photo>()
//        Timber.i("Listener attached")
//        registration = collectionRef.addSnapshotListener addSnapShotListener@{ querySnapshot, e ->
//            if (e != null) {
//                Timber.i(e, "Listen Failed")
//                return@addSnapShotListener
//            }
//            for (document in querySnapshot!!) {
//                val photo = document.toObject(Photo::class.java)
//                list.add(photo)
//                adapter.submitList(list)
//            }
//        }
//        recyclerView.adapter = adapter
//    }

    private fun setPhotoListener() {
        photoListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNullTo(photoList) {
                    it.getValue(Photo::class.java)
                }.also {
                    adapter.submitList(photoList)
                }
            }
        }
    }


    private fun detachListener() {
        registration.remove()
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber") //or use Uri.fromParts()
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun composeSmsMessage(address: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("smsto:$address")  // This ensures only SMS apps respond
            putExtra("sms_body", getString(R.string.messageHeader))
            //putExtra("address", address)
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

}