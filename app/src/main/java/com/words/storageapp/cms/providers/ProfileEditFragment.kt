package com.words.storageapp.cms.providers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.words.storageapp.R
import com.words.storageapp.database.model.LabourerDbModel
import com.words.storageapp.databinding.FragmentStatusEditBinding
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.preference.LocationFragment
import com.words.storageapp.ui.UserRepository
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

    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var profileImage: ImageView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var fireDatabase: DatabaseReference
    private lateinit var rootdocReference: DatabaseReference
    private lateinit var binding: FragmentStatusEditBinding
    private var result: FirebaseUser? = null
    private var imageBitMap: Bitmap? = null


    @Inject
    lateinit var repository: UserRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseStorage = FirebaseStorage.getInstance()
        fireDatabase = Firebase.database.reference

        arguments?.let {
            this.result = it.getParcelable<FirebaseUser>("Edit")
        }

        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(requireContext())

        setLocationRequest()
        locationCallback()
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

        binding = FragmentStatusEditBinding.inflate(
            inflater, container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        profileImage = binding.profileUpdate
        binding.uiData = result
        binding.changeIconBtn.setOnClickListener(this)
        rootdocReference = fireDatabase.child("skills").child(result?.id!!)

        binding.updateBtn.setOnClickListener {
            submitUpdate()
        }

        binding.backKey.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.locationBtn.setOnClickListener {
            binding.progressBtn.visibility = View.VISIBLE
            getLocation()
        }

        Glide.with(profileImage.context)
            .load(result?.imageUrl)
            .apply(
                RequestOptions().placeholder(R.drawable.animated_loading)
                    .error(R.drawable.ic_icon_person)
            )
            .into(profileImage)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (!checkLocationPermissionApproved()) {
            createLocationRequest()
        }
    }

    private fun checkLocationPermissionApproved() = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun startLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            , MainActivity.REQUEST_LOCATION_CODE
        )
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
        if (requestCode == MEMORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data!!.data
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, fullPhotoUri)
            binding.profileUpdate.setImageBitmap(bitmap)
            imageBitMap = bitmap
            imageBitMap?.let {
                showProgress()
                processImage(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun setLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 60000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun locationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
//                for (location in locationResult.locations) {
//                    //check if address is available if available don't call getAddress
//
//                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        //check if location permission is accepted first
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest
            , locationCallback, Looper.getMainLooper()
        )
    }

    private fun createLocationRequest() {
        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(builder?.build())

        task.addOnSuccessListener {
            lifecycleScope.launch {
            }
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        requireActivity(),
                        LocationFragment.REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.i("Cannot resolve location Error")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener(
            requireActivity(),
            OnSuccessListener { location ->
                if (location == null) {
                    Timber.i("onSuccess::null")
                    return@OnSuccessListener
                }

                val documentRef = fireDatabase.child("skills")
                    .child(result!!.id!!)

                val geoFire = GeoFire(documentRef)
                val geoLocation = GeoLocation(location.latitude, location.longitude)

                geoFire.setLocation(result!!.id, geoLocation) { _, error ->
                    if (error != null) {
                        Toast.makeText(
                            requireContext(),
                            "Error can set Laborers Location",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Location Saved",
                            Toast.LENGTH_LONG
                        ).show()

                        rootdocReference.child("latitude").setValue(location.latitude.toString())
                        rootdocReference.child("longitude").setValue(location.longitude.toString())
                        binding.progressBtn.visibility = View.GONE
                    }
                }
            })
    }

    private fun processImage(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            val imageUrlImage = startCompressing(bitmap)
            startUploadingImage(imageUrlImage)
        }
    }

//    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            ImageDecoder.decodeBitmap(
//                ImageDecoder
//                    .createSource(context.contentResolver, imageUri)
//            )
//        } else {
//            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
//                BitmapFactory.decodeStream(inputStream)
//            }
//        }
//    }

    //this method start the upload
    private fun startUploadingImage(imageByte: ByteArray?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference =
            storage.reference.child(
                "images/users/${this.result!!.id}/"
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

    //this function update the edit status Field
    private fun submitUpdate() {
        showProgress()

        val first: String? = binding.firstNameText.text.toString()
            .let { if (it.isBlank()) this.result?.firstName else it }
        val last =
            binding.lastNameText.text.toString()
                .let { if (it.isBlank()) this.result?.lastName else it }

        val mobile =
            binding.mobileBox.text.toString()
                .let { if (it.isBlank()) this.result?.mobile else it }

        val service =
            binding.serviceBox.text.toString()
                .let { if (it.isBlank()) this.result?.serviceOffered else it }
        val accountNumber =
            binding.accountNumber.text.toString()
                .let { if (it.isBlank()) this.result?.accountNumber else it }
        val accountName =
            binding.accountName.text.toString()
                .let { if (it.isBlank()) this.result?.accountName else it }
        val skill =
            binding.skillsBox.text.toString()
                .let { if (it.isBlank()) this.result?.skill else it }

//        val imageUrl: String? = imageUri?.toString() ?: userData.image_url
        rootdocReference.child("firstName").setValue(first)
        rootdocReference.child("lastName").setValue(last)
        rootdocReference.child("phone").setValue(mobile)
        rootdocReference.child("serviceOffered").setValue(service)
        rootdocReference.child("skill").setValue(skill)
        rootdocReference.child("accountName").setValue(accountName)
        rootdocReference.child("accountNumber").setValue(accountNumber)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    findNavController().navigateUp()
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