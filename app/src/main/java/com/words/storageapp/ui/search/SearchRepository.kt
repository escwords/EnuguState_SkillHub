package com.words.storageapp.ui.search

import androidx.lifecycle.LiveData
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.AllSkillAndComments
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.database.model.RecentSkillModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import javax.inject.Inject


class SearchRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {

    val skills: LiveData<List<MiniSkillModel>> = appDatabase.allSkillsDbDao().getAllSkills()

    fun getSpecificSkill(userId: String) = appDatabase.allSkillsDbDao().getSkilledUser(userId)

    // consider finding out how you can return Flow<State<List<MiniWokrData>> in the same case
    fun searchSkill(query: String) = appDatabase.allSkillsDbDao().searchSkill(query)


    fun getSkillWithComments(userId: String): LiveData<AllSkillAndComments> =
        appDatabase.allSkillsDbDao().getSkilledWithComment(userId)

    suspend fun insertIntoRecent(recent: RecentSkillModel) {
        withContext(Dispatchers.IO) {
            appDatabase.recentDao().insertRecentSkill(recent)
        }
    }

}
