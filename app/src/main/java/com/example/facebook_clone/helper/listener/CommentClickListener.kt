package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React

interface CommentClickListener {
    fun onCommentLongClicked(comment: Comment)
  //  fun onReactOnCommentClicked(commentId: String, commentPosition: Int, commentReacts: List<React>)
//    fun onReplyToCommentClicked()
    fun onMediaCommentClicked(mediaUrl: String)
}