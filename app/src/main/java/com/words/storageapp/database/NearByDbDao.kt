package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.database.model.NearByDbModel


@Dao
interface NearByDbDao {

    @Query(
        """
            SELECT DISTINCT id,skillId,first_name,locality,last_name,nearby_table.skills,image_url,
            accountState
            FROM nearby_table
            JOIN nearByFts_table ON (nearby_table.row_id = nearByFts_table.rowId) 
            WHERE nearByFts_table MATCH :query
        """
    )
    fun searchNearBy(query: String?): LiveData<List<MiniSkillModel>>


    @Query(
        """SELECT DISTINCT id,skillId,first_name,
            locality,last_name,skills,image_url
        FROM nearby_table"""
    )
    fun getAllNearBy(): LiveData<List<MiniSkillModel>>

    //execute this query in co-routine background thread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nearBy: List<NearByDbModel>)

    @Query("DELETE FROM nearby_table")
    suspend fun deleteAllUsers()

    @Transaction
    suspend fun updateAllSkill(nearBy: List<NearByDbModel>) {
        deleteAllUsers()
        insertAll(nearBy)
    }

}