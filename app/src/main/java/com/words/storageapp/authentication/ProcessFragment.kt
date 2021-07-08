package com.words.storageapp.authentication

import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.words.storageapp.R
import com.words.storageapp.domain.Photo
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.preference.PreferenceViewModel
import com.words.storageapp.cms.providers.ProfileEditFragment
import com.words.storageapp.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject


class ProcessFragment : Fragment() {

    //firebase database
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var rootDocRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var geoLocation: GeoLocation

    //private var processCounter = 1
    var uid: String? = null

    @Inject
    lateinit var prefViewModel: PreferenceViewModel

    private lateinit var regModel: PersonDetail
    private var profileImage = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        regModel = (activity as MainActivity).registerModel

        storage = FirebaseStorage.getInstance()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fireDatabase = Firebase.database.reference

        uid = fireDatabase.child("skills").push().key //generates random id Key
        rootDocRef = fireDatabase.child("skills").child(uid!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_process, container, false)

        lifecycleScope.launch {
            processProfileImage(regModel.profilePicture!!)
        }

        profileImage.observe(viewLifecycleOwner, Observer { imageUrls ->
            submitDetail(imageUrls)
        })

        prefViewModel.addresses.observe(viewLifecycleOwner, Observer {
            val addressModel = it[0]
            geoLocation = GeoLocation(addressModel.latitude!!, addressModel.longitude!!)
        })

        return view
    }

    override fun onAttach(context: Context) {
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
        super.onAttach(context)
    }

    private fun submitDetail(image: String?) {

        val labourer = with(regModel) {
            FirebaseUser(
                id = uid,
                firstName = firstName,
                lastName = lastName,
                imageUrl = image,
                locality = areaAddress,
                mobile = mobile,
                skill = skill,
                serviceOffered = skillDescription,
                skillId = userId,
                accountName = accountName,
                accountNumber = accountNumber,
                accountStatus = "noActive"
            )
        }

        Timber.i("RegisterDetail: $labourer")

        val documentRef = fireDatabase.child("skills")
        val geoFire = GeoFire(documentRef)

        documentRef.setValue(labourer).addOnSuccessListener {
            Timber.i("Account has been Created")
            geoFire.setLocation(
                uid, geoLocation
            ) { _, error ->
                if (error != null) {
                    Timber.i("Error saving location to database")
                    Toast.makeText(
                        requireContext(),
                        "Error has occurred, Try again", Toast.LENGTH_SHORT
                    ).show()
                    //deleteAccount() //try email and password if error occurred.
                    //hideLoadingProgress()
                } else {
                    Timber.i("Location saved successfully")
                    lifecycleScope.launch {
                        //ProcessListWork function was called here previously
                        Toast.makeText(
                            requireContext(), "Upload Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_processFragment_to_adminFragment)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Timber.e(e, "Failed to save User Document")
            //display Error Dialog telling user to check internet
            ///displayErrorDialog()
            firebaseAuth.currentUser!!.delete()
            Timber.i("Delete account:Success, Account deleted")
        }
    }


    //uploads the selected pictures to cloud
    private fun startUploadingWorkImage(imageByte: ByteArray?) {

        val documentRef = rootDocRef.child("works").push()


        val storageRef: StorageReference =
            storage.reference.child(
                "images/users/${documentRef.key}/"
            )

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
                    val imageUri = task.result.toString()

                    val photo = Photo(
                        documentRef.key,
                        imageUri,
                        firebaseAuth.currentUser!!.uid
                    )

                    documentRef.setValue(photo).addOnCompleteListener {
                        Toast.makeText(
                            requireContext(),
                            "Uploaded ${documentRef.key}",
                            Toast.LENGTH_SHORT
                        ).show()

//                        if (regModel.workPhotos.size
//                            == processCounter) {
//                            Toast.makeText(requireContext(),"Upload Successfully",
//                                Toast.LENGTH_SHORT).show()
//                            findNavController().navigate(R.id.action_processFragment_to_adminFragment)
//                        }else {
//                            processCounter += 1
//                        }
                    }
                }
            }
        }
    }

    private fun startUploadingProfileImage(
        imageByte: ByteArray?,
        image: MutableLiveData<String>
    ) {

        val storageRef: StorageReference =
            storage.reference.child(
                "images/users/${firebaseAuth.currentUser!!.uid}/"
            )
        //var imageUri: String? = null
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
                    val imageUri = task.result.toString()
                    image.postValue(imageUri)
                }
            }//end complete listener
        }
        // return imageUri
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

    //the function reduces the size of the image
    private suspend fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        return coroutineScope {
            val stream: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }

    private suspend fun processProfileImage(imageBitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val compressedImage = startCompressing(imageBitmap)
            startUploadingProfileImage(compressedImage, profileImage)
        }
    }

    //this function process the list of bitmap images to list of string
    private suspend fun processListImage(images: List<Bitmap>) {
        images.forEach { bitMap ->
            withContext(Dispatchers.IO) {
                val compressedImage = startCompressing(bitMap)
                startUploadingWorkImage(compressedImage)
            }
        }
    }

    private fun showLoadingProgress() {
        lifecycleScope.launch {
            // binding.loadingCard.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingProgress() {
        lifecycleScope.launch {
            //binding.loadingCard.visibility = View.GONE
        }
    }

    private fun deleteAccount() {
        firebaseAuth.currentUser!!.delete()
    }

}