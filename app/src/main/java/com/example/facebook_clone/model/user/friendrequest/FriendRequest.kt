package com.example.facebook_clone.model.user.friendrequest

import com.google.firebase.Timestamp
import java.util.*

data class FriendRequest
    (
    var fromId: String? = null,
    var toId: String? = null,
    val requestId: String? = UUID.randomUUID().toString(),
    val requestTime: Timestamp = Timestamp(Date())
)
