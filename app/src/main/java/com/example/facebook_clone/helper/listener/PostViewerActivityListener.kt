package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React

interface PostViewerActivityListener {

    fun onReactTextViewClicked(post: Post,
                               interactorId: String,
                               interactorName: String,
                               interactorImageUrl: String,
                               reacted: Boolean,
                               currentReact: React?)

    fun onReactTextViewLongClicked(post: Post,
                               interactorId: String,
                               interactorName: String,
                               interactorImageUrl: String,
                               reacted: Boolean,
                               currentReact: React?)

    fun onShowCommentsClicked(post: Post,
                              interactorId: String,
                              interactorName: String,
                              interactorImageUrl: String,
                              postPosition: Int,
                              notifiedToken: String
    )

    fun onShareButtonClicked(post: Post,
                             interactorId: String,
                             interactorName: String,
                             interactorImageUrl: String,
                             postPosition: Int)
}