package com.words.storageapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatLabourerModel(
    @PrimaryKey @ColumnInfo(name = "rowId") var id: String? = null,
    @ColumnInfo(name = "imageUrl") var imageUrl: String? = null,
    @ColumnInfo(name = "labourerName") var labourerName: String? = null,
    @ColumnInfo(name = "skill") var skills: String? = null
)