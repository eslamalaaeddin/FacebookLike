package com.example.facebook_clone.model.post

import com.example.facebook_clone.model.post.share.Share
import com.google.firebase.Timestamp
import java.util.*

//Firebase gives me an error when i use Post
data class SharedPost (
    val id: String? = UUID.randomUUID().toString(),
    var content: String? = null,
    var attachmentUrl: String? = null,//it will hold urls for media as images, videos, and docs
    var attachmentType: String? = null,
    var publisherId: String? = null,
    var publisherImageUrl: String? = null,
    var publisherName: String? = null,
    var visibility: Int? = 0, // 0 --> public, 1 --> private ......
    val creationTime: Timestamp = Timestamp(Date())
)