package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.comment.Comment


interface CommentsBottomSheetListener {
    fun onAnotherUserCommented(commentPosition: Int, commentId: String, postId: String)
}