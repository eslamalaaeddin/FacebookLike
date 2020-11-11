package com.example.facebook_clone.helper

import com.example.facebook_clone.model.post.Comment

interface CommentClickListener {
    fun onCommentLongClicked(comment: Comment)
}