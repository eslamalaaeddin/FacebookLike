package com.example.facebook_clone.model.post.react

import com.google.firebase.Timestamp
import java.util.*

data class React
    (
    val id: String? = UUID.randomUUID().toString(),
    var reactorId: String? = null,
    var reactorName: String? = null,
    var reactorImageUrl: String? = null,
    val react: Int? = 1,
    val reactTime: Timestamp = Timestamp(Date())
)
