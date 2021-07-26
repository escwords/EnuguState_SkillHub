package com.words.storageapp.ui

import androidx.lifecycle.LiveData
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.ClientDbModel
import com.words.storageapp.database.model.LabourerDbModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//scope to class to Account Activity scope
class UserRepository @Inject constructor(val database: AppDatabase)