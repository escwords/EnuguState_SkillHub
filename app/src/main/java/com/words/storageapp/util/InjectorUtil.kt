package com.words.storageapp.util

import com.words.storageapp.ui.detail.DetailViewModel.Companion.DetailViewModelFactory
import com.words.storageapp.ui.search.SearchRepository
import com.words.storageapp.ui.search.SearchViewModel.Companion.SearchViewModelFactory

object InjectorUtil {

    fun provideSearchViewModelFactory(repository: SearchRepository): SearchViewModelFactory {
        return SearchViewModelFactory(repository)
    }

    fun provideDetailViewModelFactory(
        id: String,
        repository: SearchRepository
    ): DetailViewModelFactory {
        return DetailViewModelFactory(id, repository)
    }

}