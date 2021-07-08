package com.words.storageapp.database.model

import androidx.room.*
import java.util.*

@Entity(tableName = "logged_user")
data class LabourerDbModel(
    @PrimaryKey @ColumnInfo(name = "rowId") var id: String,
    @ColumnInfo(name = "first_name") var first_name: String? = null,
    @ColumnInfo(name = "last_name") var last_name: String? = null,
    @ColumnInfo(name = "image_url") var image_url: String? = null,
    @ColumnInfo(name = "accountNumber") var accountNumber: String? = null,
    @ColumnInfo(name = "accountName") var accountName: String? = null,
    @ColumnInfo(name = "mobile") var mobile: String? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "locality") var locality: String? = null,
    @ColumnInfo(name = "skill") var skills: String? = null,
    @ColumnInfo(name = "experience") var experience: String? = null,
    @ColumnInfo(name = "serviceOffered1") var serviceOffered: String? = null, // this field contains more of what the user can do
    @ColumnInfo(name = "date_joined") var date_joined: Date? = null
)



