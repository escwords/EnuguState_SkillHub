package com.words.storageapp.util

import android.annotation.SuppressLint
import android.content.res.TypedArray
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


@SuppressLint("Recycle")
@BindingAdapter("customBackground")
fun parseBackgroundCover(view: ImageView, facultyCode: Int) {
    val colors: TypedArray = view.resources.obtainTypedArray(R.array.skill_Cat_drawables)
    val choiceColor = when (facultyCode) {
        700 -> colors.getResourceId(0, 1)
        20 -> colors.getResourceId(3, 0)
        25 -> colors.getResourceId(7, 0)
        30 -> colors.getResourceId(1, 0)
        40 -> colors.getResourceId(5, 1)
        50 -> colors.getResourceId(8, 0)
        60 -> colors.getResourceId(4, 1)
        70 -> colors.getResourceId(2, 0)
        80 -> colors.getResourceId(6, 1)
        90 -> colors.getResourceId(9, 0)
        else -> colors.getResourceId(6, 3)
    }
    view.setImageResource(choiceColor)
}

@BindingAdapter("status")
fun accountStatus(status: TextView, state: Boolean) {
    if (state) {
        status.text = "Active"
    } else {
        status.text = "InActive"
    }
}