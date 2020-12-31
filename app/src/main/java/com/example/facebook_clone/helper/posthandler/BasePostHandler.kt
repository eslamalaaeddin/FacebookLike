package com.example.facebook_clone.helper.posthandler

import com.example.facebook_clone.model.post.Post

open class BasePostHandler {

    fun locatePostDestination(
        post: Post,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    ): Post {
        post.firstCollectionType = firstCollectionType
        post.creatorReferenceId = creatorReferenceId
        post.secondCollectionType = secondCollectionType

        return post
    }
}