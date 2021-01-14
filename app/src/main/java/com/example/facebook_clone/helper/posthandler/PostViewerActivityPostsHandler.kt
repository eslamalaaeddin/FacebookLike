package com.example.facebook_clone.helper.posthandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.PostViewerActivityListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel

private const val TAG = "PostViewerActivityPosts"

class PostViewerActivityPostsHandler(
    private val context: Context,
    private val postViewModel: PostViewModel,
    private val profileActivityViewModel: ProfileActivityViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel,
    private val othersProfileActivityViewModel: OthersProfileActivityViewModel,
    private val notifiedToken: String
) : BasePostHandler(
    context,
    postViewModel,
    notificationsFragmentViewModel,
    othersProfileActivityViewModel
), PostViewerActivityListener {

    private val profileActivityPostsHandler =
        ProfileActivityPostsHandler(
            "ProfileActivity",
            context,
            postViewModel,
            profileActivityViewModel
        )

    private val othersProfileActivityPostsHandler =
        OthersProfileActivityPostsHandler(
            context,
            postViewModel,
            notificationsFragmentViewModel,
            othersProfileActivityViewModel
        )

    override fun onReactTextViewClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?
    ) {
        if (post.publisherId == interactorId) {
            profileActivityPostsHandler.onReactButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                -1
            )
            Toast.makeText(context, "To Profile", Toast.LENGTH_SHORT).show()
        }
        else {
            othersProfileActivityPostsHandler.onReactButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                -1,
                notifiedToken = notifiedToken
            )
        }
    }

    override fun onReactTextViewLongClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?
    ) {
        if (post.publisherId == interactorId) {
            profileActivityPostsHandler.onReactButtonLongClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                -1
            )
            Toast.makeText(context, "To Profile", Toast.LENGTH_SHORT).show()
        } else {
            othersProfileActivityPostsHandler.onReactButtonLongClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                -1,
                notifiedToken = notifiedToken
            )
        }
    }

    override fun onShowCommentsClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String
    ) {
        if (post.publisherId == interactorId) {
            profileActivityPostsHandler.openCommentsBottomSheet(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                postPosition,
                null
            )
            Toast.makeText(context, "To Profile", Toast.LENGTH_SHORT).show()
        } else {
            othersProfileActivityPostsHandler.openCommentsBottomSheet(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                postPosition,
                othersProfileActivityPostsHandler
            )
        }
    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        if (post.publisherId == interactorId) {
            profileActivityPostsHandler.onShareButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                postPosition
            )
            Toast.makeText(context, "To Profile", Toast.LENGTH_SHORT).show()
        } else {
            othersProfileActivityPostsHandler.onShareButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                postPosition,
                notifiedToken = notifiedToken
            )
        }
    }
}