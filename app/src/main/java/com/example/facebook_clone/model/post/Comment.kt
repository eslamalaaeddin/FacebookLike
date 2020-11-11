package com.example.facebook_clone.model.post

import com.google.firebase.Timestamp
import java.util.*

data class Comment(
    var commenterId: String? = null,
    var commenterName: String? = null,
    var commenterImageUrl: String? = null,
    var comment: String? = null,
    var commentType: String? = null,
    val commentTime: Timestamp = Timestamp(Date())
)