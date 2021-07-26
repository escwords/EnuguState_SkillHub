package com.words.storageapp.ui

import com.words.storageapp.di.Storage
import com.words.storageapp.util.CurrentLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(private val storage: Storage)