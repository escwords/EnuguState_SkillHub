package com.words.storageapp.domain

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

//Annotate with IgnoreExtra Properties and Initialize all properties to null to circumvent no constructor runtime exception
@IgnoreExtraProperties
data class User(
    var address: String? = null,
    @ServerTimestamp val date_joined: Date? = null,
    var email: String? = null,
    var fname: String? = null,
    var latitude: String? = null,
    var lname: String? = null,
    var longitude: String? = null,
    var phoneNumber: String? = null,
    var photoUrl: String? = null,
    var user_Id: String? = null
) {

    @Exclude
    override fun toString(): String {
        return "\n $fname \n $email \n $user_Id"
    }

}