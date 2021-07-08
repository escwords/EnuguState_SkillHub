package com.words.storageapp.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "address_table")
data class AddressModel(
    val latitude: Double?,
    val longitude: Double?
) {
    @PrimaryKey
    var rowId: Int = 0
}
