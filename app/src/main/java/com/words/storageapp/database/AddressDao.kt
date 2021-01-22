package com.words.storageapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.words.storageapp.database.model.AddressModel

@Dao
interface AddressDao {

    @Query("SELECT *FROM address_table")
    fun getAddress(): LiveData<List<AddressModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAddress(addressModel: AddressModel)

    @Query("DELETE FROM address_table")
    fun deleteAddresses()
}