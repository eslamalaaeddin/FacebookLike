package com.example.facebook_clone.helper


interface PostListener{
    fun onCommentClicked(postPublisherId:String,postId:String, commenterId: String,commenterName: String, imageUrl: String)
}