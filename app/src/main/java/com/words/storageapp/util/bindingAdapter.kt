package com.words.storageapp.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.words.storageapp.R
import com.words.storageapp.R.drawable.*

@BindingAdapter("imgSrcUri")
fun ImageView.convertUriToImage(imageUrl: String?) {
    imageUrl?.let {
        //val uri = it.toUri().buildUpon().scheme("https").build()
        Glide.with(this.context)
            .load(imageUrl)
            .apply(RequestOptions().placeholder(animated_loading).error(broken_img))
            .into(this)
    }
    // apply(RequestOptions().placeholder(loading_animation).error(R.drawable.ic_broken_image)
}

@BindingAdapter("status")
fun accountStatus(status: TextView, state: Boolean) {
    if (state) {
        status.text = "Active"
    } else {
        status.text = "InActive"
    }
}