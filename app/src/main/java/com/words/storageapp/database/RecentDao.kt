package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.words.storageapp.database.model.RecentSkillModel

@Dao
interface RecentDao {
    @Query("SELECT *FROM recent_table")
    fun getRecentSkill(): LiveData<List<RecentSkillModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentSkill(recentData: RecentSkillModel)
}