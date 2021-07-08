package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.CommentDbModel

@Dao
interface CommentDao {

    @Query("SELECT *FROM comment_table")
    fun getAllComment(): LiveData<List<CommentDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentDbModel>)

    @Query("DELETE FROM comment_table")
    suspend fun deleteAllUsers()

    @Transaction
    suspend fun updateAllSkill(allSkills: List<CommentDbModel>) {
        deleteAllUsers()
        insertAll(allSkills)
    }
}