package com.words.storageapp.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "address_table")
data class AddressModel(
    val address: String?,
    val locality: String?,
    val latitude: Double?,
    val longitude: Double?
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Int = 0
}
