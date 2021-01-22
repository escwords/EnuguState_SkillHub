package com.words.storageapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.words.storageapp.domain.RegisterUser

@Entity(tableName = "nearby_table")
class NearByDbModel(
    @ColumnInfo(name = "id") val id: String?,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "accountState") val accountActive: Boolean?,
    @ColumnInfo(name = "phone") val mobile: String?,
    @ColumnInfo(name = "experience") val experience: String?,
    @ColumnInfo(name = "gender") val gender: String?,
    @ColumnInfo(name = "charges") val charges: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "skillId") val skillId: String?,
    @ColumnInfo(name = "serviceOffered1") val serviceOffered1: String?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "latitude") val latitude: String?,
    @ColumnInfo(name = "longitude") val longitude: String?,
    @ColumnInfo(name = "locality") val locality: String?,
    @ColumnInfo(name = "skills") val skills: String?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "row_id")
    var rowId: Int = 0
}

@Fts4(contentEntity = NearByDbModel::class)
@Entity(tableName = "nearByFts_table")
data class NearByDbFts(
    @ColumnInfo(name = "skills") val skills: String?
)

fun RegisterUser.toNearBySkill(): NearByDbModel {
    return NearByDbModel(
        id = skillId,
        firstName = firstName,
        lastName = lastName,
        email = email,
        mobile = phone,
        experience = experience,
        gender = gender,
        imageUrl = imageUrl,
        serviceOffered1 = serviceOffered,
        skills = skills,
        locality = locality,
        skillId = skillId,
        charges = wageRate,
        latitude = latitude,
        longitude = longitude,
        date = null,
        accountActive = accountActive
    )
}