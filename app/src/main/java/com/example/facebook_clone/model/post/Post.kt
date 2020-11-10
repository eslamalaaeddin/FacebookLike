package com.example.facebook_clone.model.post

import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.HashMap

data class Post(
    val id: String? = UUID.randomUUID().toString(),
    var content: String? = null,
    var mediaContent: String? = null,//it will hold urls for media as images, videos, and docs
    var mediaType: String? = null,//video, or image.....
    var comments: List<Comment>? = null,
    var reacts: List<React>? = null,
    var shares: List<Share>? = null,
    var publisherId: String? = null,
    var publisherImageUrl: String? = null,
    var publisherName: String? = null,
    var visibility: Int? = 0, // 0 --> public, 1 --> private ......
//    var isShared: Boolean? = false,
    val creationTime: Timestamp = Timestamp(Date())
)