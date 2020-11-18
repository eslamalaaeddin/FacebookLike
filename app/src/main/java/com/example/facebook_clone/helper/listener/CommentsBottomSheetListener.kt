package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React


interface CommentsBottomSheetListener {
    fun onAnotherUserCommented(commentPosition: Int, commentId: String, postId: String)
   // fun onReactOnCommentClicked(commentId: String, commentPosition: Int, commentReacts: List<React>)
}