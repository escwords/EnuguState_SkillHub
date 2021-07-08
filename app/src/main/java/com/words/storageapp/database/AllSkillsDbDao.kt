package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.AllSkillAndComments
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.database.model.AllSkillsDbModel

@Dao
interface AllSkillsDbDao {

    @Query(
        """
            SELECT all_skills.*
            FROM fts_table
            JOIN all_skills ON (fts_table.rowId =rowId) 
            WHERE fts_table MATCH :query
        """
    )
    fun searchSkill(query: String): LiveData<List<MiniSkillModel>>

    @Transaction
    @Query(
        """
        SELECT *FROM all_skills WHERE id = :userId
    """
    )
    fun getSkilledWithComment(userId: String): LiveData<AllSkillAndComments>

    @Query("SELECT * FROM all_skills WHERE id = :userId")
    fun getSkilledUser(userId: String): LiveData<AllSkillsDbModel>

    @Query(" SELECT *FROM all_skills")
    fun getAllSkills(): LiveData<List<MiniSkillModel>>

    //execute this query in co-routine background thread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(allSkills: List<AllSkillsDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allSkills: AllSkillsDbModel)

    @Update
    suspend fun update(allSkills: AllSkillsDbModel)

    @Query("DELETE FROM all_skills")
    suspend fun deleteAllUsers()

    @Transaction
    suspend fun updateAllSkill(allSkills: List<AllSkillsDbModel>) {
        deleteAllUsers()
        insertAll(allSkills)
    }

}
