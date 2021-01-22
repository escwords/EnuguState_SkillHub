package com.words.storageapp.database.model

import androidx.room.*

@Entity(tableName = "all_skills")
data class AllSkillsDbModel(
    @ColumnInfo(name = "id") val id: String?,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "accountState") val accountActive: Boolean?,
    @ColumnInfo(name = "phone") val mobile: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "experience") val experience: String?,
    @ColumnInfo(name = "education") val education: String?,
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

data class Coordinate(
    @ColumnInfo(name = "latitude") val latitude: String?,
    @ColumnInfo(name = "longitude") val longitude: String?
)

@Fts4(contentEntity = AllSkillsDbModel::class)
@Entity(tableName = "fts_table")
data class AllSkillsFts(
    @ColumnInfo(name = "locality") val locality: String?,
    @ColumnInfo(name = "skills") val skills: String?
)

data class MiniSkillModel(
    @ColumnInfo(name = "id") val id: String?,
    @ColumnInfo(name = "skillId") val skillId: String?,
    @ColumnInfo(name = "first_name") val first_name: String?,
    @ColumnInfo(name = "accountState") val accountActive: Boolean?,
    @ColumnInfo(name = "last_name") val last_name: String?,
    @ColumnInfo(name = "skills") val skills: String?,
    @ColumnInfo(name = "locality") val locality: String?,
    @ColumnInfo(name = "image_url") val image_url: String?
)

//make sure that the data your fetching is  mapped with columnInfo annotation
data class DetailWokrData(
    @ColumnInfo(name = "row_id") val rowId: Int?,
    @ColumnInfo(name = "first_name") val first_name: String?,
    @ColumnInfo(name = "last_name") val last_name: String?,
    @ColumnInfo(name = "phone") val mobile: String?,
    @ColumnInfo(name = "accountState") val accountActive: Boolean?,
    @ColumnInfo(name = "skills") val skills: String?,
    @ColumnInfo(name = "image_url") val image_url: String?,
    @ColumnInfo(name = "locality") val locality: String?,
    @ColumnInfo(name = "skillId") val skillId: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "gender") val gender: String?,
    @ColumnInfo(name = "serviceOffered1") val serviceOffered1: String?,
    @ColumnInfo(name = "experience") val experience: String?,
    @ColumnInfo(name = "charges") val charges: String?
)
