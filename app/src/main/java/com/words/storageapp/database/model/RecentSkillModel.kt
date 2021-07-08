package com.words.storageapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_table")
class RecentSkillModel(
    @ColumnInfo(name = "id") @PrimaryKey val id: String,
    @ColumnInfo(name = "img_Url") val imgUrl: String?,
    @ColumnInfo(name = "firstName") val firstName: String?,
    @ColumnInfo(name = "lastName") val lastName: String?,
    @ColumnInfo(name = "skill") val skill: String?
)