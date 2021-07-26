package com.words.storageapp.database.model

import androidx.room.*

@Entity(tableName = "all_skills", indices = [Index(value = ["id"], unique = true)])
data class AllSkillsDbModel(
    @ColumnInfo(name = "id") var id: String, //don't assign rowId to this variable, resulted to constraint error
    @ColumnInfo(name = "firstName") var firstName: String?,
    @ColumnInfo(name = "lastName") var lastName: String?,
    @ColumnInfo(name = "accountName") var accountName: String?,
    @ColumnInfo(name = "accountNumber") var accountNumber: String?,
    @ColumnInfo(name = "accountStatus") var accountStatus: String?,
    @ColumnInfo(name = "phone") var mobile: String?,
    @ColumnInfo(name = "imageUrl") var imageUrl: String?,
    @ColumnInfo(name = "skillId") var skillId: String?,
    @ColumnInfo(name = "serviceOffered") var serviceOffered: String?,
    @ColumnInfo(name = "starNum") var starNum: Double?,
    @ColumnInfo(name = "date") var date: Long?,
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "longitude") var longitude: Double?,
    @ColumnInfo(name = "locality") var locality: String?,
    @ColumnInfo(name = "skill") var skill: String?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowId")
    var rowId: Int = 0
}

@Entity(
    tableName = "comment_table",
    foreignKeys = [ForeignKey(
        entity = AllSkillsDbModel::class,
        parentColumns = ["id"],
        childColumns = ["laborerId"]
    )],
    indices = [Index("laborerId")]
)
data class CommentDbModel(
    @ColumnInfo(name = "commentId") var commentId: String,
    @ColumnInfo(name = "laborerId") var laborerId: String,
    @ColumnInfo(name = "comment") var comment: String?,
    @ColumnInfo(name = "starNum") var starNum: Double?,
    @ColumnInfo(name = "timeStamp") var timeStamp: Long?,
    @ColumnInfo(name = "authorId") var authorId: String?,
    @ColumnInfo(name = "authorFName") var authorFName: String?,
    @ColumnInfo(name = "authorUrl") var authorUrl: String?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowId")
    var rowId = 0
}

data class AllSkillAndComments(
    @Embedded var allSkills: AllSkillsDbModel,
    @Relation(parentColumn = "id", entityColumn = "laborerId")
    var comments: List<CommentDbModel>
)

@Fts4(contentEntity = AllSkillsDbModel::class)
@Entity(tableName = "fts_table")
data class AllSkillsFts(
    @ColumnInfo(name = "skillId") var skillId: String,
    @ColumnInfo(name = "firstName") var firstName: String?,
    @ColumnInfo(name = "lastName") var lastName: String?,
    @ColumnInfo(name = "skill") var skill: String?,
    @ColumnInfo(name = "locality") var locality: String?,
    @ColumnInfo(name = "starNum") var starNum: Double?
)

data class MiniSkillModel(
    var id: String?,
    var skillId: String?,
    var firstName: String?,
    var accountStatus: String?,
    var lastName: String?,
    var skill: String?,
    var locality: String?,
    var imageUrl: String?,
    var latitude: Double?,
    var longitude: Double?,
    var serviceOffered: String?
)

