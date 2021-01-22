package com.words.storageapp.ui.detail

import androidx.lifecycle.*
import com.words.storageapp.database.model.DetailWokrData
import com.words.storageapp.database.model.AllSkillsDbModel
import com.words.storageapp.ui.search.SearchRepository
import timber.log.Timber

class DetailViewModel(val id: String, private val repository: SearchRepository) : ViewModel() {

    val detailData: LiveData<AllSkillsDbModel> = liveData {
        emitSource(repository.getSpecificSkill(id))
    }

    private val detailData1: LiveData<DetailWokrData> = liveData {
        emitSource(repository.getSkill(id))
    }

    val userFullName = detailData1.map {
        "${it.first_name} ${it.last_name}"
    }

    val imageUrl = detailData1.map { detail ->
        detail.image_url
    }

    val mobile = detailData.map { detail ->
        detail.mobile
    }

    val serviceOffered1 = detailData.map { detail ->
        detail.serviceOffered1
    }.also { Timber.i("ServiceOffered1: ${it.value}") }

    val experience = detailData.map { detail ->
        detail.experience
    }

    val charges = detailData.map { detail ->
        detail.charges
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