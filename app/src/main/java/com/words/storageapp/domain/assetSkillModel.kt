package com.words.storageapp.domain

import android.os.Parcelable
import com.words.storageapp.database.model.AllSkillsDbModel
import com.words.storageapp.database.model.CommentDbModel
import kotlinx.android.parcel.Parcelize

data class AssetSkillModel(
    val id: String,
    val accountStatus: String,
    val firstName: String,
    val lastName: String,
    val imageUrl: String,
    val accountNumber: String,
    val accountName: String,
    val mobile: String,
    val skillId: String,
    val serviceOffered: String,
    val date: Long,
    val comments: List<AssetComment>?,
    val starNum: Double,
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val skill: String
)

@Parcelize
data class AssetComment(
    val commentId: String?,
    val laborerId: String?,
    val authorId: String?,
    val author: String?,
    val authorUrl: String?,
    val comment: String?,
    val starNum: Double?,
    var timeStamp: Long
) : Parcelable

fun List<AssetSkillModel>.toAllSkillModel():
        List<AllSkillsDbModel> {
    return this.map {
        AllSkillsDbModel(
            id = it.id,
            firstName = it.firstName,
            lastName = it.lastName,
            accountStatus = it.accountStatus,
            mobile = it.mobile,
            imageUrl = it.imageUrl,
            skillId = it.skillId,
            serviceOffered = it.serviceOffered,
            starNum = it.starNum,
            date = it.date,
            latitude = it.latitude,
            longitude = it.longitude,
            locality = it.locality,
            skill = it.skill,
            accountNumber = it.accountNumber,
            accountName = it.accountName
        )
    }
}

fun List<AssetComment>.toCommentDb(): List<CommentDbModel> {
    return this.map {
        CommentDbModel(
            commentId = it.commentId!!,
            laborerId = it.laborerId!!,
            comment = it.comment,
            starNum = it.starNum,
            timeStamp = it.timeStamp,
            authorId = it.authorId,
            authorFName = it.author,
            authorUrl = it.authorUrl
        )
    }
}

fun List<CommentDbModel>.toDomainComment()
        : List<AssetComment> {
    return this.map {
        AssetComment(
            comment = it.comment,
            laborerId = it.laborerId,
            commentId = it.commentId,
            authorUrl = it.authorUrl,
            authorId = it.authorId,
            starNum = it.starNum,
            author = "${it.authorFName}",
            timeStamp = it.timeStamp!!
        )
    }
}


