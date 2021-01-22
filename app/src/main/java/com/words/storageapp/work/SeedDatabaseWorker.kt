package com.words.storageapp.work

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.AllSkillsDbModel
import com.words.storageapp.util.SKILLS_JSON_DATA
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

//For inserting dummy data to the database.
class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open(SKILLS_JSON_DATA).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val skillsType = object : TypeToken<List<AllSkillsDbModel>>() {}.type
                    val skillList: List<AllSkillsDbModel> = Gson().fromJson(jsonReader, skillsType)

                    val database = AppDatabase.getInstance(applicationContext)
                    database.allSkillsDbDao().insertAll(skillList)
                    Timber.i("Database seeding was successful \n $skillList")
                    Result.success()
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Error seeding database")
            Result.failure()
        }
    }

    companion object {
        private val TAG = SeedDatabaseWorker::class.java.simpleName
    }
}