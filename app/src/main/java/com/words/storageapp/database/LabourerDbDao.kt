package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.LabourerDbModel

@Dao
interface LabourerDbDao {

    @Query("SELECT *FROM logged_user")
    fun getUser(): LiveData<LabourerDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LabourerDbModel)

    @Query("DELETE FROM logged_user")
    suspend fun deleteUser()

    @Transaction
    suspend fun setUpAccount(loggedInUser: LabourerDbModel) {
        deleteUser()
        insertUser(loggedInUser)
    }

    @Update
    suspend fun update(loggedInUser: LabourerDbModel)

}