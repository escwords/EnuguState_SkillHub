package com.words.storageapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.words.storageapp.domain.FirebaseUser


@Entity(tableName = "client_table")
data class ClientDbModel(
    @PrimaryKey @ColumnInfo(name = "rowId") var id: String,
    @ColumnInfo(name = "first_name") var first_name: String? = null,
    @ColumnInfo(name = "last_name") var last_name: String? = null,
    @ColumnInfo(name = "imageUrl") var imageUrl: String? = null,
    @ColumnInfo(name = "locality") var locality: String? = null,
    @ColumnInfo(name = "accountType") var accountType: String? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "phoneNumber") var phoneNumber: String? = null,
    @ColumnInfo(name = "gender") var gender: String? = null,
    @ColumnInfo(name = "date") var date: Long? = null
)


fun FirebaseUser.toClientDbModel(): ClientDbModel {
    return ClientDbModel(
        id = skillId!!,
        first_name = firstName,
        last_name = lastName,
        imageUrl = imageUrl,
        phoneNumber = mobile
    )
}