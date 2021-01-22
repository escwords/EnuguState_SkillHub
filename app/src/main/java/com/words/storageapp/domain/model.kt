package com.words.storageapp.domain

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatData(
    val clientId: String? = null,
    val clientName: String? = null,
    val imageUrl: String? = null
)
