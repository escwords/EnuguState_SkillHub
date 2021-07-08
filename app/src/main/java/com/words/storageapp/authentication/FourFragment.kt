package com.words.storageapp.authentication

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
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.words.storageapp.R
import com.words.storageapp.cms.providers.ProfileEditFragment
import com.words.storageapp.ui.main.MainActivity
import timber.log.Timber

class FourFragment : Fragment() {
    lateinit var registerFragment: RegisterFragment
    private lateinit var profileImage: ImageView
    private lateinit var selectBtn: ImageView
    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_four, container, false)
        val nextBtn = view.findViewById<MaterialButton>(R.id.nextBtn)
        val prevBtn = view.findViewById<ImageView>(R.id.prevBtn)
        profileImage = view.findViewById(R.id.profileImage)
        selectBtn = view.findViewById(R.id.selectImage)

        nextBtn.setOnClickListener {
            if (imageBitmap == null) {
                Toast.makeText(
                    requireContext(),
                    "Select Picture to Upload", Toast.LENGTH_LONG
                ).show()
            } else {
                (activity as MainActivity).registerModel.apply {
                    profilePicture = imageBitmap
                }
                registerFragment.navigateToProcess()
            }
        }

        prevBtn.setOnClickListener {
            registerFragment.moveToPrevPager()
        }

        selectBtn.setOnClickListener {
            openStorageIntent()
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
            startActivityForResult(intent, ProfileEditFragment.MEMORY_REQUEST_CODE)
        }
    }

    //this function  returns the result from the camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.i("done selecting photo")
        if (requestCode == ProfileEditFragment.MEMORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data!!.data
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, fullPhotoUri)
            profileImage.setImageBitmap(bitmap)
            imageBitmap = bitmap
            //processImage(fullPhotoUri)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(_registerFragment: RegisterFragment) =
            FourFragment().apply {
                registerFragment = _registerFragment
            }
    }
}