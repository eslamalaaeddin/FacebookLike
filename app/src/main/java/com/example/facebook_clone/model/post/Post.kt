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
    var attachmentUrl: String? = null,
    var attachmentType: String? = null,
    var comments: List<Comment>? = null,
    var commentsAvailable: Boolean = true,
    var reacts: List<React>? = null,
    var shares: MutableList<Share>? = null,
    var publisherId: String? = null,
    var publisherImageUrl: String? = null,
    var publisherName: String? = null,
    var visibility: Int? = 0,
    val creationTime: Timestamp = Timestamp(Date()),
    var firstCollectionType: String = "",
    var creatorReferenceId: String = "",
    var secondCollectionType: String = "",
    var groupName: String? = null,
    var groupId: String? = null

)
