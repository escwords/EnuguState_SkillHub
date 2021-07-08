package com.words.storageapp.laborer.viewProfile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.words.storageapp.adapters.DeleteClickListener
import com.words.storageapp.adapters.EditPhotoListAdapter
import com.words.storageapp.cms.providers.ProfileEditFragment
import com.words.storageapp.databinding.FragmentEditAlbumBinding
import com.words.storageapp.domain.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream

class EditAlbumFragment : Fragment() {

    companion object {
        const val MEMORY_REQUEST_CODE = 110
        const val MB = 1000000.0
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var registration: ListenerRegistration
    private lateinit var imageProg: ProgressBar
    private lateinit var noResult: ImageView
    private lateinit var adapter: EditPhotoListAdapter
    private lateinit var recyclerView: RecyclerView


    //firebase database
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var collectionReference: DatabaseReference
    private lateinit var dataListener: ValueEventListener

    private val list = MutableLiveData<List<Photo>>()
    private val listData: MutableList<Photo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        //firebaseFirestore = FirebaseFirestore.getInstance()

        val userId = firebaseAuth.currentUser!!.uid

        firebaseDatabase = Firebase.database.reference
        collectionReference = firebaseDatabase.child("skills")
            .child(userId).child("works")

        setUpDataListener()

        //collectionRef = firebaseFirestore.collection(getString(R.string.db_node)).document(userId).collection("photos")
        Timber.i("userId:$userId")
        collectionReference.addValueEventListener(dataListener)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentEditAlbumBinding.inflate(inflater, container, false)
        binding.addImage.setOnClickListener { openMemoryIntent() }
        imageProg = binding.imageProgressBar
        recyclerView = binding.imageList
        noResult = binding.noResult
        showImageProgress()

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = EditPhotoListAdapter(DeleteClickListener { photoId ->
            showImageProgress()
            collectionReference.child(photoId).removeValue()
        })

        recyclerView.adapter = adapter
        list.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                hideImageProgress()
                noResult.visibility = View.VISIBLE
            } else {
                hideImageProgress()
                noResult.visibility = View.GONE
                adapter.submitList(it)
            }
        })
        //attachPhotoListener()
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        collectionReference.removeEventListener(dataListener)
    }

    private fun openMemoryIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(
                intent,
                MEMORY_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MEMORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Timber.i("Done Selecting photo from external storage")
            val fullPhotoUri: Uri? = data!!.data
            //callback!!.getImageBitMap(fullPhotoUri)
            processImage(fullPhotoUri)
        }
    }

    private fun processImage(uri: Uri?) {
        showImageProgress()

        if (uri != null) {
            //val source: ImageDecoder.Source = ImageDecoder.createSource(requireContext().contentResolver,uri)
            val bitmap: Bitmap? = getBitmap(requireContext(), uri)
            lifecycleScope.launch(Dispatchers.IO) {
                bitmap?.let {
                    val imageUrlImage = startCompressing(it)
                    startUploadingImage(imageUrlImage)
                }
            }
        }
    }

    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder
                    .createSource(context.contentResolver, imageUri)
            )
        } else {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private suspend fun startCompressing(bitmap: Bitmap): ByteArray? {
        return withContext(Dispatchers.Default) byte@{
            var bytes: ByteArray? = null

            for (i in 1 until 11) {
                if (i == 10) {
                    Timber.i("Image is too large")
                    Toast.makeText(activity, "Image is Too Large", Toast.LENGTH_LONG)
                        .show()
                    break
                }
                bytes = getBytesFromBitmap(bitmap, 100 / i)
                if (bytes.size / ProfileEditFragment.MB < ProfileEditFragment.MB_THRESHOLD) {
                    return@byte bytes
                }
            }
            bytes
        }
    }

    //this method start the upload
    private fun startUploadingImage(imageByte: ByteArray?) {

        val userId = firebaseAuth.currentUser!!.uid
        Timber.i("UserId: = $userId")

        val documentRef = collectionReference.push()

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpg")
            .setContentLanguage("en")
            .build()

        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference.child(
            "images/users/${documentRef.key}/"
        )

        imageByte?.let { imageByteArray ->
            val uploadTask = storageRef.putBytes(imageByteArray, metadata)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { e ->
                        Timber.e(e, "Error uploading Image")
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.i("upload was successful")

                    val url: Uri? = task.result

                    val photo = Photo(
                        documentRef.key,
                        url.toString(),
                        firebaseAuth.currentUser!!.uid
                    )
                    documentRef.setValue(photo).addOnSuccessListener {
                        Timber.i(" Photo update was Successfully")
                    }.addOnFailureListener { e ->
                        Timber.e(e, "Failed to update photo on FireStore")
                        hideImageProgress()
                    }
                } else {
                    Timber.i("Failed to Upload Image")
                    hideImageProgress()
                }
            }
        }
    }

//    private fun attachPhotoListener() {
//        val list = mutableListOf<Photo>()
//
//        registration = collectionRef.addSnapshotListener addSnapShotListener@{ querySnapshot, e ->
//            if (e != null) {
//                Timber.i(e, "Listen Failed")
//                return@addSnapShotListener
//            }
//            for (document in querySnapshot!!) {
//                Timber.i("Listening to Photos$ list")
//                val photo = document.toObject(Photo::class.java)
//                list.add(photo)
//
//            }
//            hideImageProgress()
//            adapter.submitList(list)
//        }
//        // hideImageProgress()
//    }

    private fun setUpDataListener() {
        dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.i("Listening to Photos $snapshot")
                snapshot.children.mapNotNullTo(listData) {
                    it.getValue(Photo::class.java)
                }.also {
                    list.postValue(listData)
                }
                //listData.mapTo(list){ photo ->   }
                Timber.i("Returning Photos$ $listData")
                // hideImageProgress()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
    }


    private suspend fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        return coroutineScope {
            val stream: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }

    private fun showImageProgress() {
        lifecycleScope.launchWhenResumed {
            imageProg.visibility = View.VISIBLE
        }
    }

    private fun hideImageProgress() {
        lifecycleScope.launchWhenResumed {
            imageProg.visibility = View.GONE
        }
    }

}

