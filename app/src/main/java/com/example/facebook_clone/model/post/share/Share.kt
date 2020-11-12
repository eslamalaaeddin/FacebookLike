package com.example.facebook_clone.model.post.share

import com.google.firebase.Timestamp
import java.util.*

data class Share
    (
    val id: String? = UUID.randomUUID().toString(),
    var sharerId: String? = null,
    var sharerName: String? = null,
    var sharerImageUrl: String? = null,
    val sharerTime: Timestamp = Timestamp(Date())
)
