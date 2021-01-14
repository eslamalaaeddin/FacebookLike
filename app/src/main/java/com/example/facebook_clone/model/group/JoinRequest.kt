package com.example.facebook_clone.model.group

import com.google.firebase.Timestamp
import java.util.*

data class JoinRequest(
    val requestId: String? = UUID.randomUUID().toString(),
    var requester: Member? = null,
    val requestTime:Timestamp = Timestamp(Date())
)