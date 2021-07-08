package com.words.storageapp.preference

import androidx.lifecycle.*
import com.words.storageapp.database.AppDatabase
import javax.inject.Inject

class PreferenceViewModel @Inject constructor(val db: AppDatabase) : ViewModel() {
    val addresses = liveData {
        emitSource(db.addressDao().getAddress())
    }

    val addressString = Transformations.map(addresses) { it ->
        it.map { }
    }
}