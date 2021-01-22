package com.words.storageapp.ui.search

import androidx.lifecycle.*
import com.words.storageapp.database.model.LabourerDbModel
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.database.model.NearByDbModel
import com.words.storageapp.domain.NearBySkill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val result = MutableLiveData<String>()
    val queryString: LiveData<String>
        get() = result

    val allNearBySkills =
        liveData<List<MiniSkillModel>> {
            emitSource(searchRepository.getAllNearBy())
        }

    private var currentJob: Job? = null

    private val _filter = MutableLiveData<String>()

    val nearByFiltered = _filter.switchMap { query ->
        when (query) {
            "all" -> allNearBySkills
            else -> searchRepository.nearBySkills(query)
        }
    }


    val allSkills = liveData<List<MiniSkillModel>> {
        emitSource(searchRepository.skills)
    }

    //observed in searchFragment
    val skills = result.switchMap { query ->
        searchRepository.searchSkill2(query)
    }.distinctUntilChanged()


    val nearBySkills = MutableLiveData<List<MiniSkillModel>>()
    fun searchNearByInit(query: String?) {
        currentJob?.cancel()
        _filter.value = query
        currentJob = viewModelScope.launch {
            try {
                nearBySkills.postValue(
                    when (_filter.value) {
                        null -> {
                            allNearBySkills.value
                        }
                        else -> {
                            allNearBySkills.value!!.filter { it.skills == query }
                        }
                    })
            } catch (e: IOException) {
                nearBySkills.value = emptyList<MiniSkillModel>()
            }
        }
    }

    //wrapping the query to liveData
    fun queryDb(query: String) {
        result.value = query
    }

    fun nearByQuery(query: String) {
        _filter.postValue(query)
    }


    fun initNearByTable(skills: List<NearByDbModel>) {
        viewModelScope.launch {
            searchRepository.updateNearByData(skills)
        }
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
