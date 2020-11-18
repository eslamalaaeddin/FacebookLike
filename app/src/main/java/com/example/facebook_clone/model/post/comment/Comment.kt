package com.example.facebook_clone.model.post.comment

import com.example.facebook_clone.model.post.react.React
import com.google.firebase.Timestamp
import java.util.*

data class Comment(
    val id: String? = UUID.randomUUID().toString(),
    var commenterId: String? = null,
    var commenterName: String? = null,
    var commenterImageUrl: String? = null,
    var attachmentCommentUrl: String? = null,
    var textComment: String? = null,
    var commentType: String? = null,
    var subComments: List<Comment>? = null,
    var reacts: List<React>? = null,
    val commentTime: Timestamp = Timestamp(Date())
)