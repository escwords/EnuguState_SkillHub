package com.words.storageapp.domain

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties

@Parcelize
data class StartData(
    val id: Int,
    val name: String,
    val index: Int,
    val cats: List<String>
) : Parcelable

data class RecentData(
    val id: Int,
    val imgUrl: String,
    val fullName: String,
    val skill: String
)
