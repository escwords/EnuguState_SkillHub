package com.words.storageapp.client

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.words.storageapp.R
import com.words.storageapp.cms.providers.ProfileEditFragment
import com.words.storageapp.cms.providers.ProfileEditFragment.Companion.MEMORY_REQUEST_CODE
import com.words.storageapp.domain.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import timber.log.Timber
import java.io.ByteArrayOutputStream

class ClientEditFragment : Fragment() {

    var client: FirebaseUser? = null
    private lateinit var profileImage: ImageView
    private var imageBitMap: Bitmap? = null
    private lateinit var rootdocReference: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var firebase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            client = it.get("edit_client") as FirebaseUser?
        }
        firebase = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_edit, container, false)
        val saveBtn = view.findViewById<MaterialButton>(R.id.updateBtn)
        profileImage = view.findViewById<ImageView>(R.id.profilePics_edit)
        firstName = view.findViewById<TextInputEditText>(R.id.firstNameText)
        lastName = view.findViewById<TextInputEditText>(R.id.lastNameText)
        val backKey = view.findViewById<ImageView>(R.id.backKey)
        val changeBtn = view.findViewById<ImageView>(R.id.changeIconBtn)
        progressBar = view.findViewById(R.id.progressBar)
        rootdocReference = firebase.child("clients").child(client?.skillId!!)

        Glide.with(profileImage.context)
            .load(client?.imageUrl)
            .into(profileImage)

        client?.let {
            firstName.text = it.firstName
            lastName.text = it.lastName
        }

        changeBtn.setOnClickListener {
            openStorageIntent()
        }

        saveBtn.setOnClickListener {
            submit()
            findNavController().navigateUp()
        }

        backKey.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    //Note no request  for Memory permission is defined here
    private fun openStorageIntent() {
        Timber.i("memory intent requested")
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

    //this function  returns the result from the camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.i("done selecting photo")
        if (requestCode == MEMORY_REQUEST_CODE &&
            resultCode == Activity.RESULT_OK
        ) {
            val fullPhotoUri: Uri? = data!!.data
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, fullPhotoUri)
            profileImage.setImageBitmap(bitmap)
            imageBitMap = bitmap
            imageBitMap?.let {
                showProgress()
                processImage(it)
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            val imageUrlImage = startCompressing(bitmap)
            startUploadingImage(imageUrlImage)
        }
    }

    //this method start the upload
    private fun startUploadingImage(imageByte: ByteArray?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference =
            storage.reference.child(
                "images/users/${client?.id}/"
            )

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
                    val imageUri = task.result
                    imageUri?.let { uri ->
                        documentReference.setValue(uri.toString()).addOnSuccessListener {
                            //when the submit button is clicked the new image flicks try loading the new selected image into bitmap
                            Glide.with(profileImage.context)
                                .load(uri)
                                .apply(
                                    RequestOptions().placeholder(R.drawable.animated_loading)
                                        .error(R.drawable.ic_icon_person)
                                )
                                .into(profileImage)

                            hideProgress()
                            Toast.makeText(
                                requireContext(), "Image updated",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                        }.addOnFailureListener {
                            Toast.makeText(
                                requireContext(), "Upload Failed",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } else {
                    Timber.i("Upload failed")
                }
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

    private suspend fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        return coroutineScope {
            val stream: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }

    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    private fun submit() {
        showProgress()

        val first: String? = firstName.text.toString().let {
            if (it.isBlank()) client?.firstName else it
        }

        val last: String? = lastName.text.toString().let {
            if (it.isBlank()) client?.lastName else it
        }

        rootdocReference.child("firstName").setValue(first)
        rootdocReference.child("lastName").setValue(last)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    Toast.makeText(
                        requireContext(), "Update Successful ",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }

    }
}