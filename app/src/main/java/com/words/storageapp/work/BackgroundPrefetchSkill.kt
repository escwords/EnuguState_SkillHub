package com.words.storageapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.domain.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.Exception

class BackgroundPrefetchSkill(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val Work_Name = "BackgroundPrefetchSkill"
    }

    override suspend fun doWork(): Result = coroutineScope {
        val firebaseDb = Firebase.database.reference
        val localDb = AppDatabase.getInstance(applicationContext)

        val skillDbPath = firebaseDb.child("skills")

        try {

            skillDbPath.get().addOnSuccessListener { snapShot ->
                snapShot.children.mapNotNull {
                    it.getValue(FirebaseUser::class.java)?.toAllSkillsModel()
                }.also { skills ->
                    CoroutineScope(Dispatchers.IO).launch {
                        Timber.i("laborers: $skills")
                        localDb.allSkillsDbDao().insertAll(skills)
                    }
                }
            }
            Timber.i("Background Prefetch Success")
            Result.success()

        } catch (ex: Exception) {
            Timber.i("Background Prefetch Error : ${ex.message}")
            Result.retry()
        }
    }

}