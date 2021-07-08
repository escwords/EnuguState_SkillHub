package com.words.storageapp.cms.complaint

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@IgnoreExtraProperties
@Parcelize
data class FirebaseComplaint(
    var author: String? = null,
    var text: String? = null,
    var mobile: String? = null,
    @ServerTimestamp var timeStamp: Long? = System.currentTimeMillis()
) : Parcelable


