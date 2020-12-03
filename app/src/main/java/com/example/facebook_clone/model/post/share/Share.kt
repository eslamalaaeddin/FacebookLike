package com.example.facebook_clone.model.post.share

import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.SharedPost
import com.google.firebase.Timestamp
import java.util.*

data class Share
    (
    val id: String? = UUID.randomUUID().toString(),
    var sharerId: String? = null,
    var sharerName: String? = null,
    var sharerImageUrl: String? = null,
    var sharedPost: SharedPost? = null,//Yoyo's post
    val shareTime: Timestamp = Timestamp(Date())
)
