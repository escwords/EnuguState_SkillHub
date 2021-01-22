package com.words.storageapp.ui.account.user

import androidx.lifecycle.*
import com.words.storageapp.database.model.LabourerDbModel

//This class fetches  logged user data from user Repository and exposes it between two UI
// viewProfile and EditProfile
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    val userData: LiveData<LabourerDbModel> = liveData {
        emitSource(repository.loggedUser)
    }

}