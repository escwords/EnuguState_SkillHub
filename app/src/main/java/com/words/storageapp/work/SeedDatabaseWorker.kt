package com.words.storageapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.domain.AssetSkillModel
import com.words.storageapp.domain.toAllSkillModel
import com.words.storageapp.domain.toCommentDb
import com.words.storageapp.util.FILE
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
                    val skillsType = object : TypeToken<List<AssetSkillModel>>() {}.type
                    val skillList: List<AssetSkillModel> = Gson().fromJson(jsonReader, skillsType)

                    val database = AppDatabase.getInstance(applicationContext)
                    val skillData = skillList.toAllSkillModel()
                    database.allSkillsDbDao().insertAll(skillData)

//                    val comments = skillList.flatMap { asset ->
//                        asset?.comments!!
//                    }
                    skillList.forEach {
                        it.comments?.let { asset ->
                            Timber.i("asset: $asset")
                            database.commentDao().insertAll(asset.toCommentDb())
                        }
                    }
                    Result.success()
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Error seeding database")
            Result.retry()
        }
    }
}