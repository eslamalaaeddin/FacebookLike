package com.example.facebook_clone.helper.listener


interface PostListener{

    fun onReactButtonClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean
    )

    fun onReactButtonLongClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean
    )

    fun onCommentButtonClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    )

    fun onShareButtonClicked(
        postPublisherId:String,
        postId:String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    )
}