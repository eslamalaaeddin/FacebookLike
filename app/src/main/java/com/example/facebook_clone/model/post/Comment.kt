package com.example.facebook_clone.model.post

data class Comment(
    var publisherId: String? = null,
    var publisherName: String? = null,
    var publisherImageUrl: String? = null,
    var comment: String? = null,
    var commentType: String? = null,
)