package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.react.React


interface PostListener{

    fun onReactButtonClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postReacts: List<React>?,
        postPosition: Int
    )

    fun onReactButtonLongClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postReacts: List<React>?,
        postPosition: Int
    )

    fun onCommentButtonClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    )

    fun onShareButtonClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    )

    fun onReactLayoutClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    )

    fun onMediaPostClicked(mediaUrl: String)
}