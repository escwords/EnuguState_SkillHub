package com.words.storageapp.domain

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.words.storageapp.database.model.LabourerDbModel
import com.words.storageapp.database.model.AllSkillsDbModel
import java.util.*

fun RegisterUser.toLoggedInUser(): LabourerDbModel {
    return LabourerDbModel(
        id = skillId ?: "",
        first_name = firstName,
        last_name = lastName,
        email = email,
        accountActive = accountActive,
        image_url = imageUrl,
        mobile = phone,
        address = address,
        state = state,
        lga = lga,
        locality = locality,
        dob = dob,
        gender = gender,
        wageRate = wageRate,
        skills = skills,
        experience = experience,
        serviceOffered1 = serviceOffered,
        serviceOffered2 = serviceOffered2,
        date_joined = date_joined
    )
}


fun RegisterUser.toWokrData(): AllSkillsDbModel {
    return AllSkillsDbModel(
        id = skillId,
        firstName = this.firstName,
        lastName = lastName,
        email = email,
        mobile = phone,
        address = address,
        experience = experience,
        education = education,
        accountActive = accountActive,
        gender = gender,
        charges = wageRate,
        imageUrl = imageUrl,
        skillId = skillId,
        serviceOffered1 = serviceOffered,
        date = date_joined.toString(),
        latitude = latitude,
        longitude = longitude,
        locality = locality,
        skills = skills
    )
}

//register user data model
@IgnoreExtraProperties
data class RegisterUser(
    var address: String? = null,
    var accountStatus: Int? = null,
    var accountInfo: String? = null,
    var accountType: String? = null,
    @ServerTimestamp var date_joined: Date? = null,
    var dob: String? = null,
    var education: String? = null,
    var accountActive: Boolean? = null,
    var email: String? = null,
    var experience: String? = null,
    var firstName: String? = null,
    var gender: String? = null,
    var imageUrl: String? = null,
    var lastName: String? = null,
    var lga: String? = null,
    var locality: String? = null,
    var phone: String? = null,
    var serviceOffered: String? = null, // this field contains more of what the user can do
    var serviceOffered2: String? = null,
    var skillId: String? = null,
    var skills: String? = null,
    var state: String? = null,
    var wageRate: String? = null,
    var latitude: String? = null,
    var longitude: String? = null
)

@IgnoreExtraProperties
data class NearBySkill(
    val firstName: String? = null,
    val lastName: String? = null,
    val locality: String? = null,
    val skillId: String? = null
)

@IgnoreExtraProperties
data class Photo(
    var photoId: String? = null,
    var photoUrl: String? = null,
    var userId: String? = null,
    @ServerTimestamp var uploadTime: Date? = null
)

@IgnoreExtraProperties
data class EditImageData(
    var firstName: String? = null,
    var imageUrl: String? = null,
    var lastName: String? = null,
    var locality: String? = null,
    var wageRate: String? = null,
    var skill: String? = null,
    var serviceOffered: String? = null,
    var phone: String? = null,
    var serviceOffered2: String? = null,
    var experience: String? = null
)