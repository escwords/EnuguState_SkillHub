package com.words.storageapp.ui.search

import androidx.lifecycle.*
import com.words.storageapp.database.model.MiniSkillModel
import javax.inject.Inject


class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val result = MutableLiveData<String>()

    private val _filter = MutableLiveData<String>()

    val allSkills = liveData<List<MiniSkillModel>> {
        emitSource(searchRepository.skills)
    }

    //observed in searchFragment
    val skills = result.switchMap { query ->
        searchRepository.searchSkill(query)
    }.distinctUntilChanged()

    //wrapping the query to liveData
    fun queryDb(query: String) {
        result.value = query
    }

    companion object {
        class SearchViewModelFactory(
            private val repository: SearchRepository
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>) =
                SearchViewModel(repository) as T
        }
    }
}
