package com.words.storageapp.di.dagger

import android.content.Context
import com.words.storageapp.skills.SkillFragment
import com.words.storageapp.home.StartFragment
import com.words.storageapp.authentication.ProcessFragment
import com.words.storageapp.preference.LocationFragment
import com.words.storageapp.preference.PreferenceFragment
import com.words.storageapp.laborer.LoginFragment
import com.words.storageapp.laborer.viewProfile.ProfileFragment
import com.words.storageapp.cms.providers.ProfileEditFragment
import com.words.storageapp.skills.SkillsItemFragment
import com.words.storageapp.ui.detail.SkilledFragment
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.ui.search.SearchFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [SearchComponentModule::class, DataStorageModule::class])
interface AppLevelComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppLevelComponent
    }

    fun inject(mainActivity: MainActivity)
    fun inject(fragment: ProcessFragment)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: ProfileEditFragment)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: SearchFragment)
    fun inject(fragment: SkilledFragment)
    fun inject(fragment: LocationFragment)
    fun inject(fragment: PreferenceFragment)
    fun inject(fragment: SkillFragment)
    fun inject(fragment: StartFragment)
    fun inject(fragment: SkillsItemFragment)
    fun searchComponent(): SearchComponent.Factory

}