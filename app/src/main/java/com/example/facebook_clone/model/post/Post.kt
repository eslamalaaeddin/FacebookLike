package com.example.facebook_clone.model.post

import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.google.firebase.Timestamp
import java.util.*

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
    val creationTime: Timestamp = Timestamp(Date())
)