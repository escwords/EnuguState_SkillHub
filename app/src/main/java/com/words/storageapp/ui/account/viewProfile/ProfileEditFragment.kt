package com.words.storageapp.ui.account.viewProfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.words.storageapp.R
import com.words.storageapp.database.model.LabourerDbModel
import com.words.storageapp.databinding.FragmentStatusEditBinding
import com.words.storageapp.domain.EditImageData
import com.words.storageapp.ui.account.user.UserRepository
import com.words.storageapp.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject


//Done with this function remaining test running.
class ProfileEditFragment : Fragment(), View.OnClickListener {

    companion object {
        const val MEMORY_REQUEST_CODE = 180
        const val MB = 1000000.0
        const val MB_THRESHOLD = 0.47
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage


    private lateinit var firebaseFirestore: FirebaseFirestore


    private lateinit var profileImage: ImageView
    private lateinit var documentRef: DocumentReference
    private lateinit var userId: String
    private var imageUri: Uri? = null


    private lateinit var fireDatabase: DatabaseReference
    private lateinit var rootdocReference: DatabaseReference


    private lateinit var binding: FragmentStatusEditBinding
    var result: EditImageData? = null

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var repository: UserRepository

    private lateinit var userData: LabourerDbModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        fireDatabase = Firebase.database.reference

        val currentUser = firebaseAuth.currentUser
        userId = currentUser!!.uid
        documentRef =
            firebaseFirestore.collection(getString(R.string.fireStore_node)).document(userId)
        rootdocReference = fireDatabase.child("skills").child(userId)
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

        binding = FragmentStatusEditBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        profileImage = binding.profilePicsEdit
        binding.uiData = profileViewModel

        binding.changeIconBtn.setOnClickListener(this)

        binding.updateBtn.setOnClickListener {
            submitUpdate()
        }
        binding.backKey.setOnClickListener {
            findNavController().navigateUp()
        }

        profileViewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            //call initialize with data to set up the ui
            userData = user
            user?.let {
                Glide.with(requireContext())
                    .load(it.image_url)
                    .centerCrop()
                    .into(profileImage)
            }
        })

        return binding.root
    }

    //Note no request  for Memory permission is defined here
    private fun openStorageIntent() {
        Timber.i("memory intent requested")
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, MEMORY_REQUEST_CODE)
        }
    }

    //this function  returns the result from the camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.i("done selecting photo")
        if (requestCode == MEMORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data!!.data
            // val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,fullPhotoUri)
            processImage(fullPhotoUri)
        }
    }

    private fun processImage(uri: Uri?) {
        if (uri != null) {
            // val source: Source = ImageDecoder.createSource(requireContext().contentResolver,uri)
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

    //this method start the upload
    private fun startUploadingImage(imageByte: ByteArray?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference =
            storage.reference.child(
                "images/users/${firebaseAuth.currentUser!!.uid}/"
            )

//        val documentRef = firebaseFirestore.collection(getString(R.string.fireStore_node))
//            .document(firebaseAuth.currentUser!!.uid)

        val documentReference = rootdocReference.child("imageUrl")

        imageByte?.let { imageByteArray ->
            val uploadTask = storageRef.putBytes(imageByteArray)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { e ->
                        Timber.e(e, "Error uploading Image")
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageUri = task.result
                    imageUri?.let {
                        documentReference.setValue(it.toString()).addOnSuccessListener {
                            //when the submit button is clicked the new image flicks try loading the new selected image into bitmap
                            Glide.with(requireContext())
                                .load(imageUri)
                                .centerCrop()
                                .into(profileImage)
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Timber.i("Upload failed")
                }
            }
        }
    }

    //this function update the edit status Field
    private fun submitUpdate() {

        showProgress()

        val first: String? = binding.firstNameText.text.toString()
            .let { if (it.isBlank()) userData.first_name else it }
        val last =
            binding.lastNameText.text.toString()
                .let { if (it.isBlank()) userData.last_name else it }

        val localityNew = binding.localityNameText.text.toString()
            .let { if (it.isBlank()) userData.locality else it }

        val wage =
            binding.wageBox.text.toString()
                .let { if (it.isBlank()) userData.wageRate else it }

        val mobileNew =
            binding.mobileBox.text.toString()
                .let { if (it.isBlank()) userData.mobile else it }

        val service =
            binding.serviceBox.text.toString()
                .let { if (it.isBlank()) userData.serviceOffered1 else it }
        val service2 =
            binding.service1Box.text.toString()
                .let { if (it.isBlank()) userData.serviceOffered2 else it }
        val experienceNew =
            binding.experienceBox.text.toString()
                .let { if (it.isBlank()) userData.experience else it }

        val skill =
            binding.skillsBox.text.toString()
                .let { if (it.isBlank()) userData.skills else it }

//        val imageUrl: String? = imageUri?.toString() ?: userData.image_url
        rootdocReference.child("firstName").setValue(first)
        rootdocReference.child("lastName").setValue(last)
        rootdocReference.child("locality").setValue(localityNew)
        rootdocReference.child("wageRate").setValue(wage)
        rootdocReference.child("phone").setValue(mobileNew)
        rootdocReference.child("serviceOffered").setValue(service)
        rootdocReference.child("skills").setValue(skill)
        rootdocReference.child("serviceOffered2").setValue(service2)
        rootdocReference.child("experience").setValue(experienceNew)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    val action = R.id.action_profileImageFragment_to_profileFragment
                    findNavController().navigate(action)
                    Timber.i("Clicked")
                }
            }
    }

    ///This function Iterate the size of the images to 480kb before upload
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
                if (bytes.size / MB < MB_THRESHOLD) {
                    return@byte bytes
                }
            }
            bytes
        }
    }

    //the function reduces the size of the image
    private suspend fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        return coroutineScope {
            val stream: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.changeIcon_btn -> openStorageIntent()
        }
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}