package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.ClientDbModel

@Dao
interface ClientDbDao {

    @Query("SELECT *FROM client_table ")
    fun getClient(): LiveData<ClientDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClient(clientDbModel: ClientDbModel)

    @Update
    fun updateClient(clientDbModel: ClientDbModel)

    @Delete
    fun deleteClient(clientDbModel: ClientDbModel)

    @Transaction
    suspend fun setUpAccount(clientDbModel: ClientDbModel) {
        deleteClient(clientDbModel)
        insertClient(clientDbModel)
    }
}