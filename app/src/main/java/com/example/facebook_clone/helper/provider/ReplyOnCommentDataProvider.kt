package com.example.facebook_clone.helper.provider

import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React

interface ReplyOnCommentDataProvider {
    fun reactOnCommentFromRepliesDataProvider(
        superComment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?,
        clickType: String
    )
}