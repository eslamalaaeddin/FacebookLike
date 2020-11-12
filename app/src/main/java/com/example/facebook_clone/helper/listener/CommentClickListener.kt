package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.comment.Comment

interface CommentClickListener {
    fun onCommentLongClicked(comment: Comment)
}