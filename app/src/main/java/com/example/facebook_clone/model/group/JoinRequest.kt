package com.example.facebook_clone.model.group

import com.google.firebase.Timestamp
import java.util.*

data class JoinRequest(
    var requestId: String? = null,
    var requester: Member,
    val requestTime:Timestamp = Timestamp(Date())
)