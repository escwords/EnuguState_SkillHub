package com.words.storageapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "logged_user")
data class LabourerDbModel(
    @PrimaryKey @ColumnInfo(name = "rowId") var id: String,
    @ColumnInfo(name = "first_name") var first_name: String? = null,
    @ColumnInfo(name = "last_name") var last_name: String? = null,
    @ColumnInfo(name = "email_address") var email: String? = null,
    @ColumnInfo(name = "image_url") var image_url: String? = null,
    @ColumnInfo(name = "mobile") var mobile: String? = null,
    @ColumnInfo(name = "accountState") var accountActive: Boolean?,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "state") var state: String? = null,
    @ColumnInfo(name = "locality") var locality: String? = null,
    @ColumnInfo(name = "lga") var lga: String? = null,
    @ColumnInfo(name = "dob") var dob: String? = null,
    @ColumnInfo(name = "gender") var gender: String? = null,
    @ColumnInfo(name = "minimum_charge") var wageRate: String? = null,
    @ColumnInfo(name = "skills") var skills: String? = null,
    @ColumnInfo(name = "experience") var experience: String? = null,
    @ColumnInfo(name = "serviceOffered1") var serviceOffered1: String? = null, // this field contains more of what the user can do
    @ColumnInfo(name = "serviceOffered2") var serviceOffered2: String? = null, // this field contains more of what the user can do
    @ColumnInfo(name = "date_joined") var date_joined: Date? = null
)

/*
fun List<LoggedInUser>.toProfileDataModel(): List<ProfileModel>{
    return map { loggedInUser ->
        ProfileModel(
            id = loggedInUser.id!!,
            firstName = loggedInUser.first_name
        )
        }
    }*/
