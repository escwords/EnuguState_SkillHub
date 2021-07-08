package com.words.storageapp.domain

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties

data class StartData(
    val id: Int,
    val name: String,
    val index: Int
)

data class RecentData(
    val id: Int,
    val imgUrl: String,
    val fullName: String,
    val skill: String
)
