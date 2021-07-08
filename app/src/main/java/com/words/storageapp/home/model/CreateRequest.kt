package com.words.storageapp.home.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@IgnoreExtraProperties
@Parcelize
data class CreateRequest(
    val author: String? = null,
    val phoneNumber: String? = null,
    val requestText: String? = null,
    val requestType: String? = null,
    val location: String? = null,
    @ServerTimestamp var date: Long? = System.currentTimeMillis()
) : Parcelable