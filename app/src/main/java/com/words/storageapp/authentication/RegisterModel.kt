package com.words.storageapp.authentication

import android.graphics.Bitmap
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class PersonDetail(
    var userId: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var mobile: String? = null,
    var email: String? = null,
    var skill: String? = null,
    var skillDescription: String? = null,
    var areaAddress: String? = null,
    var accountName: String? = null,
    var accountNumber: String? = null,
    var profilePicture: Bitmap? = null
//    var workPhotos: List<Bitmap>
)

data class Comment(
    val id: String? = null,
    val author: String? = null,
    val text: String? = null,
    @ServerTimestamp val date: Date? = null
)

