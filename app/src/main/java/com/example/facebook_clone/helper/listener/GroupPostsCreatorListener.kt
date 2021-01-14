package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.Post

interface GroupPostsCreatorListener {
    fun onGroupPostCreated(post: Post)
}