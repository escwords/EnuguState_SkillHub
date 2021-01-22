package com.words.storageapp.di.dagger

import dagger.Subcomponent

@SearchScope
@Subcomponent
interface SearchComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SearchComponent
    }

}