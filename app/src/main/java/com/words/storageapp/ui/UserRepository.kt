package com.words.storageapp.ui

import androidx.lifecycle.LiveData
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.ClientDbModel
import com.words.storageapp.database.model.LabourerDbModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//scope to class to Account Activity scope
class UserRepository @Inject constructor(
    val database: AppDatabase
) {

//    val loggedUser: LiveData<LabourerDbModel> = database.labourerDbDao().getUser()
//    val clientUser: LiveData<ClientDbModel> = database.clientDbDao().getClient()
//
//    suspend fun updateUser(user: LabourerDbModel): Boolean {
//        return withContext(Dispatchers.IO) {
//            database.labourerDbDao().update(user)
//            true
//        }
//    }
//
//    suspend fun insert(user: LabourerDbModel) {
//        withContext(Dispatchers.IO) {
//            database.labourerDbDao().insertUser(user)
//        }
//    }
//
//    suspend fun delete(): Boolean {
//        return withContext(Dispatchers.IO) {
//            database.labourerDbDao().deleteUser()
//            true
//        }
//    }
//
//    suspend fun setUpAccount(user: LabourerDbModel): Boolean {
//        return withContext(Dispatchers.IO) {
//            database.labourerDbDao().setUpAccount(user)
//            true
//        }
//    }

    suspend fun setUpClientAccount(clientDbModel: ClientDbModel): Boolean {
        return withContext(Dispatchers.IO) {
            database.clientDbDao().setUpAccount(clientDbModel)
            true
        }
    }


}