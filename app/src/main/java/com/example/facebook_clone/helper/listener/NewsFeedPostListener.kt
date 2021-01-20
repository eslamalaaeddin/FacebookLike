package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.Post

interface NewsFeedPostListener {
    fun onUserImageClicked(post: Post)
    fun onUserNameClicked(post: Post)
    fun onGroupOrPageNameClicked(post: Post)
    fun onMediaClicked(mediaUrl: String)

}