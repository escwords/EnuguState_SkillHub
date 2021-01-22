package com.words.storageapp.ui.search

import androidx.lifecycle.LiveData
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.database.model.NearByDbModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import javax.inject.Inject


class SearchRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {

    val skills: LiveData<List<MiniSkillModel>> = appDatabase.allSkillsDbDao().getAllSkills()

    //retrieve the query to skilled labourers surrounding the user

    fun getSkill(userId: String) = appDatabase.allSkillsDbDao().getSkill(userId)

    fun getSpecificSkill(userId: String) = appDatabase.allSkillsDbDao().getSkilledUser(userId)

    // consider finding out how you can return Flow<State<List<MiniWokrData>> in the same case
    fun searchSkill2(query: String) = appDatabase.allSkillsDbDao().searchSkill(query)

    fun resultSkills(query: String) = appDatabase.allSkillsDbDao().searchSkillFlow(query)

    fun nearBySkills(skill: String?) = appDatabase.nearbyDbDao().searchNearBy(skill)

    fun getAllNearBy() = appDatabase.nearbyDbDao().getAllNearBy()

    //    suspend fun insertAll(skills: List<NearByDbModel>) =
//        withContext(Dispatchers.IO) {
//        appDatabase.nearbyDbDao().insertAll(skills)
//    }
//
    suspend fun updateNearByData(skills: List<NearByDbModel>) =
        withContext(Dispatchers.IO) {
            appDatabase.nearbyDbDao().updateAllSkill(skills)
        }
}
