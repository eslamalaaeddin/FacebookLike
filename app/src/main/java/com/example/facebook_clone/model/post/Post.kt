package com.example.facebook_clone.model.post

import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import okhttp3.internal.Util
import java.util.*
import kotlin.collections.HashMap

data class Post(
    var id: String? = UUID.randomUUID().toString(),
    var content: String? = null,
    var attachmentUrl: String? = null,//it will hold urls for media as images, videos, and docs
    var attachmentType: String? = null,//video, or image.....
    var comments: List<Comment>? = null,
    var reacts: List<React>? = null,
    var shares: MutableList<Share>? = null,
    var publisherId: String? = null,
    var publisherImageUrl: String? = null,
    var publisherName: String? = null,
    var visibility: Int? = 0, // 0 --> public, 1 --> private ......
    val creationTime: Timestamp = Timestamp(Date()),
   // var publisherToken: String? = null,
//    @get:Exclude var firstCollectionType: String = "",
//    @get:Exclude var creatorReferenceId: String = "",
//    @get:Exclude var secondCollectionType: String = "",
    var firstCollectionType: String = "",
    var creatorReferenceId: String = "",
    var secondCollectionType: String = "",
    var groupName: String? = null,
    var groupId: String? = null

)
