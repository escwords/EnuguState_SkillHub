package com.words.storageapp.ui.detail

import androidx.lifecycle.*
import com.words.storageapp.database.model.AllSkillAndComments
import com.words.storageapp.database.model.AllSkillsDbModel
import com.words.storageapp.database.model.CommentDbModel
import com.words.storageapp.database.model.RecentSkillModel
import com.words.storageapp.domain.AssetComment
import com.words.storageapp.domain.toDomainComment
import com.words.storageapp.ui.search.SearchRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailViewModel(
    val id: String,
    private val repository: SearchRepository
) : ViewModel() {

    val detailData: LiveData<AllSkillsDbModel> = liveData {
        emitSource(repository.getSpecificSkill(id))
    }

    val skillData = liveData<AllSkillAndComments> {
        emitSource(repository.getSkillWithComments(id))
    }

    val imageUrl = detailData.map { detail ->
        detail.imageUrl
    }

    val rating = skillData.map { detail ->
        detail.allSkills.starNum?.toFloat() ?: 3F
    }

    val ratingAvg = skillData.map { detail ->
        detail.allSkills.starNum.toString()
    }

    val mobile = detailData.map { detail ->
        detail.mobile
    }

    fun insertInRecentTable(recentModel: RecentSkillModel) {
        viewModelScope.launch {
            repository.insertIntoRecent(recentModel)
        }
    }

    companion object {

        class DetailViewModelFactory(
            val id: String,
            private val repository: SearchRepository
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>) =
                DetailViewModel(id, repository) as T
        }
    }
}