package com.words.storageapp.di.dagger

import android.content.Context
import com.words.storageapp.preference.LocationFragment
import com.words.storageapp.preference.PreferenceFragment
import com.words.storageapp.ui.account.LoginFragment
import com.words.storageapp.ui.account.RegistrationFragment
import com.words.storageapp.ui.account.viewProfile.ProfileFragment
import com.words.storageapp.ui.account.viewProfile.ProfileEditFragment
import com.words.storageapp.ui.detail.SkilledFragment
import com.words.storageapp.ui.main.HomeFragment
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.ui.main.NearByFragment
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
    fun inject(fragment: HomeFragment)
    fun inject(fragment: RegistrationFragment)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: ProfileEditFragment)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: SearchFragment)
    fun inject(fragment: SkilledFragment)
    fun inject(fragment: LocationFragment)
    fun inject(fragment: PreferenceFragment)
    fun inject(fragment: NearByFragment)
    fun searchComponent(): SearchComponent.Factory

}