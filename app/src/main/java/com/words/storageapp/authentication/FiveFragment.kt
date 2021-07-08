package com.words.storageapp.authentication

import android.annotation.SuppressLint
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.words.storageapp.R
import com.words.storageapp.authentication.FiveFragment.ImagesListAdapter.*
import com.words.storageapp.cms.providers.ProfileEditFragment
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.util.utilities.ItemClickListener
import timber.log.Timber


class FiveFragment : Fragment() {

    private lateinit var registerFragment: RegisterFragment
    private val photos = mutableListOf<Bitmap>()
    private val observablePhotos = MutableLiveData<List<Bitmap>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_five, container, false)
        val finish = view.findViewById<MaterialButton>(R.id.finishBtn)
        val prevBtn = view.findViewById<ImageView>(R.id.prevBtn)
        val addImageBtn = view.findViewById<ImageView>(R.id.addImageBtn)
        val bitMapRecycler = view.findViewById<RecyclerView>(R.id.photosBitmap)
        val pickImage = view.findViewById<MaterialButton>(R.id.pickImage)

        finish.setOnClickListener {
            if (photos.isNotEmpty()) {
                (activity as MainActivity).registerModel.apply {
                    // workPhotos = photos
                }
                registerFragment.navigateToProcess()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Add at least One Work Photo", Toast.LENGTH_SHORT
                ).show()
            }
        }

        prevBtn.setOnClickListener {
            registerFragment.moveToPrevPager()
        }

        addImageBtn.setOnClickListener {
            openStorageIntent()
        }

        pickImage.setOnClickListener {
            openStorageIntent()
        }



        observablePhotos.observe(viewLifecycleOwner, Observer {
            val adapter = ImagesListAdapter(it)
            bitMapRecycler.adapter = adapter
        })

        return view
    }

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
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver, fullPhotoUri
            )

            photos.add(bitmap)
            observablePhotos.postValue(photos)
        }
    }

    class ImagesListAdapter(
        private val bitMaps: List<Bitmap>,
        val listener: ItemClickListener<Bitmap>? = null
    ) : RecyclerView.Adapter<ImagesListViewModel>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ImagesListViewModel {
            val inflater = LayoutInflater.from(parent.context)
            return ImagesListViewModel(inflater.inflate(R.layout.item_image_layout, parent, false))
        }

        override fun getItemCount(): Int = bitMaps.size

        override fun onBindViewHolder(
            holder: ImagesListViewModel,
            position: Int
        ) {
            val bitmap = bitMaps[position]
            holder.bind(bitmap, null)
        }

        inner class ImagesListViewModel(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

            private val picture: ImageView = itemView.findViewById(R.id.picture)

            @SuppressLint("Recycle")
            fun bind(bitmap: Bitmap, listener: ItemClickListener<Bitmap>?) {
                itemView.setOnClickListener {
                    //listener.clickAction(startData)
                }
                with(picture) {
                    setImageBitmap(bitmap)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(_registerFragment: RegisterFragment) =
            FiveFragment().apply {
                registerFragment = _registerFragment
            }
    }
}