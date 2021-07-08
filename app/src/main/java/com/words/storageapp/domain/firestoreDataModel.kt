package com.words.storageapp.domain

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.words.storageapp.database.model.LabourerDbModel
import com.words.storageapp.database.model.AllSkillsDbModel
import com.words.storageapp.database.model.CommentDbModel
import kotlinx.android.parcel.Parcelize


fun FirebaseUser.toAllSkillsModel(): AllSkillsDbModel {
    return AllSkillsDbModel(
        id = skillId!!,
        accountStatus = accountStatus,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        imageUrl = imageUrl,
        skillId = skillId,
        serviceOffered = serviceOffered,
        date = timeStamp,
        latitude = latitude,
        longitude = latitude,
        locality = locality,
        skill = skill,
        starNum = starNum,
        accountName = accountName,
        accountNumber = accountNumber
    )
}

fun List<FirebaseFetchUser>.toAllSkillsModel(): List<AllSkillsDbModel> {
    return this.map {
        AllSkillsDbModel(
            id = it.skillId!!,
            accountStatus = it.accountStatus,
            firstName = it.firstName,
            lastName = it.lastName,
            mobile = it.mobile,
            imageUrl = it.imageUrl,
            skillId = it.skillId,
            serviceOffered = it.serviceOffered,
            date = it.timeStamp,
            latitude = it.latitude,
            longitude = it.latitude,
            locality = it.locality,
            skill = it.skill,
            starNum = it.starNum,
            accountName = it.accountName,
            accountNumber = it.accountNumber
        )
    }
}

@IgnoreExtraProperties
@Parcelize
data class FirebaseUser(
    var id: String? = null,
    var accountStatus: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var imageUrl: String? = null,
    var accountNumber: String? = null,
    var accountName: String? = null,
    var locality: String? = null,
    var mobile: String? = null,
    var serviceOffered: String? = null, // this field contains more of what the user can do
    var skillId: String? = null,
    var skill: String? = null,
    var starNum: Double? = 2.0,
    var latitude: Double? = null,
    var longitude: Double? = null,
    @ServerTimestamp var timeStamp: Long = System.currentTimeMillis()
) : Parcelable

@IgnoreExtraProperties
data class FirebaseFetchUser(
    var id: String? = null,
    var accountStatus: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var imageUrl: String? = null,
    var accountNumber: String? = null,
    var accountName: String? = null,
    var locality: String? = null,
    var mobile: String? = null,
    var serviceOffered: String? = null, // this field contains more of what the user can do
    var skillId: String? = null,
    var skill: String? = null,
    var comments: List<FirebaseComment>? = null,
    var starNum: Double? = 2.0,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var timeStamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
@Parcelize
data class FirebaseContract(
    var accountName: String? = null,
    var accountNumber: String? = null,
    var contractId: String? = null,
    var clientId: String? = null,
    var laborerFName: String? = null,
    var laborerLName: String? = null,
    var laborerId: String? = null,
    var laborerUrl: String? = null,
    var skill: String? = null,
    var time: Long = System.currentTimeMillis()
) : Parcelable


fun List<FirebaseComment>.toCommentsDb(): List<CommentDbModel> {
    return this.map {
        CommentDbModel(
            commentId = it.commentId!!,
            laborerId = it.laborerId!!,
            authorFName = it.author,
            authorId = it.authorId,
            authorUrl = it.authorUrl,
            comment = it.comment,
            starNum = it.starNum,
            timeStamp = it.timeStamp
        )
    }
}

@IgnoreExtraProperties
data class FirebaseComment(
    var commentId: String? = null,
    var laborerId: String? = null,
    var authorId: String? = null,
    var author: String? = null,
    var authorUrl: String? = null,
    var comment: String? = null,
    var starNum: Double? = null,
    var timeStamp: Long = System.currentTimeMillis()
)

fun FirebaseComment.toCommentDb(): CommentDbModel {
    return CommentDbModel(
        commentId = commentId!!,
        laborerId = laborerId!!,
        authorId = authorId,
        authorFName = author,
        authorUrl = authorUrl,
        comment = comment,
        starNum = starNum,
        timeStamp = timeStamp
    )
}

@IgnoreExtraProperties
data class Photo(
    var photoId: String? = null,
    var photoUrl: String? = null,
    var userId: String? = null,
    var uploadTime: Long = System.currentTimeMillis()
)

fun FirebaseUser.toLoggedInUser(): LabourerDbModel {
    return LabourerDbModel(
        id = skillId!!,
        first_name = firstName,
        last_name = lastName,
        image_url = imageUrl,
        mobile = mobile,
        locality = locality,
        skills = skill,
        serviceOffered = serviceOffered
    )
}