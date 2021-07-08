package com.words.storageapp.client

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class MessageData(
    val messageId: String? = null,
    val senderId: String? = null,
    val messageText: String? = null
)


