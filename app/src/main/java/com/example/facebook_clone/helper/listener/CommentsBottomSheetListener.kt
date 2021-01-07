package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React


interface CommentsBottomSheetListener {
    fun onAnotherUserCommented(
        notifierId: String,
        notifierName: String,
        notifierImageUrl: String,
        notifiedId: String,
        notifiedToken: String,
        notificationType: String,
        postPublisherId: String,
        postId: String ,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentId: String
    )
   // fun onReactOnCommentClicked(commentId: String, commentPosition: Int, commentReacts: List<React>)
}