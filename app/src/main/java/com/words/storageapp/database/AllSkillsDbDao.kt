package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.DetailWokrData
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.database.model.AllSkillsDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AllSkillsDbDao {

    @Query(
        """
            SELECT DISTINCT id,skillId,first_name,all_skills.locality,last_name,all_skills.skills,image_url,accountState
            FROM all_skills
            JOIN fts_table ON (all_skills.row_id = fts_table.rowId) 
            WHERE fts_table MATCH :query
        """
    )
    fun searchSkill(query: String): LiveData<List<MiniSkillModel>>

    @Query(
        """
            SELECT DISTINCT id,skillId,first_name,all_skills.locality,last_name,all_skills.skills,image_url,accountState
            FROM all_skills
            JOIN fts_table ON (all_skills.row_id = fts_table.rowId) 
            WHERE fts_table MATCH :query
            """
    )
    fun searchSkillFlow(query: String): Flow<List<MiniSkillModel>>

    @Query(
        """
        SELECT DISTINCT row_id,
        skillId,first_name,last_name,
        all_skills.skills,phone,image_url,
        serviceOffered1,
        gender, experience,charges,locality,address
        FROM all_skills WHERE id = :userId
    """
    )
    fun getSkill(userId: String): LiveData<DetailWokrData>


    @Query("SELECT * FROM all_skills WHERE id = :userId")
    fun getSkilledUser(userId: String): LiveData<AllSkillsDbModel>

    @Query(
        """SELECT id,skillId,first_name,last_name,skills,image_url
        FROM all_skills"""
    )
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
