package com.words.storageapp.ui.main

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.words.storageapp.database.AppDatabase
import org.imperiumlabs.geofirestore.GeoFirestore
import java.lang.Appendable
import javax.inject.Inject

class StartViewModel @Inject constructor(
    appDatabase: AppDatabase
) : ViewModel() {
    val recentSkill = appDatabase.recentDao().getRecentSkill()
}