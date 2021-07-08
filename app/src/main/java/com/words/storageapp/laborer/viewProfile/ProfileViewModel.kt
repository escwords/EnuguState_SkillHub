package com.words.storageapp.laborer.viewProfile

import androidx.lifecycle.*
import com.words.storageapp.database.model.LabourerDbModel
import com.words.storageapp.domain.Photo
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.ui.UserRepository
import com.words.storageapp.util.utilities.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    val userData: LiveData<LabourerDbModel> = liveData {
//        emitSource(repository.loggedUser)
    }

    private val _createSuccess = MutableLiveData<Boolean>()
    val createSuccess: LiveData<Boolean>
        get() = _createSuccess

    val user = MutableLiveData<FirebaseUser>()
    private val serverPhotos = MutableLiveData<List<Photo>>()

    val isAcctActive: LiveData<Event<Boolean?>> = userData.map {
        Event(true)
    }

    fun initLaborerAcct(loggedInUser: LabourerDbModel?) {
        viewModelScope.launch {
            if (loggedInUser != null) {
//               repository.setUpAccount(loggedInUser)
            }
        }
    }

    fun updateAccount(loggedInUser: LabourerDbModel?) {
        viewModelScope.launch {
            if (loggedInUser != null) {
//                repository.insert(loggedInUser)
            }
        }
    }

}