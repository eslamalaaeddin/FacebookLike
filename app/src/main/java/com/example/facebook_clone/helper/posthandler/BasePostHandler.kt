package com.example.facebook_clone.helper.posthandler

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.PostConfigurationsBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.fragment.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.activity.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*

open class BasePostHandler(
    private val context: Context,
    private val postViewModel: PostViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel? = null,
    private val othersProfileActivityViewModel: OthersProfileActivityViewModel? = null,
) {
    var progressDialog: ProgressDialog? = null
    var currentEditedPostPosition: Int = -1

    private lateinit var notificationsHandler: NotificationsHandler
    init {
        if (notificationsFragmentViewModel != null && othersProfileActivityViewModel != null){
            notificationsHandler =
                NotificationsHandler(notificationsFragmentViewModel = notificationsFragmentViewModel,
                    othersProfileActivityViewModel = othersProfileActivityViewModel)
        }
    }


    fun handlePostLocation(
        post: Post,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    ): Post {
        post.firstCollectionType = firstCollectionType
        post.creatorReferenceId = creatorReferenceId
        post.secondCollectionType = secondCollectionType

        return post
    }

    fun handleMediaClicks(mediaUrl: String) {
        //Image
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show(
                (context as AppCompatActivity).supportFragmentManager,
                "signature"
            )
            imageViewerDialog.setMediaUrl(mediaUrl)
        }
        //video
        else {
            val videoIntent = Intent(context, VideoPlayerActivity::class.java)
            videoIntent.putExtra("videoUrl", mediaUrl)
            context.startActivity(videoIntent)
        }
    }
    
    fun createReact(
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    ): React {
        return React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl
        )
    }

    fun addReactOnPostToDb(interactorId: String, react: React, post: Post, notificationsHandler: NotificationsHandler? = null) {
        postViewModel.addReactToDB(react, post).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (interactorId != post.publisherId) {
                    notificationsHandler?.let { it.handleNotificationCreationAndFiring() }
                    Toast.makeText(context, "Notify Him", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun buildNotificationHandlerForPostComments(
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
    ): NotificationsHandler{
        return notificationsHandler.apply{
            this.notifierId = notifierId
            this.notifierName = notifierName
            this.notifierImageUrl = notifierImageUrl
            this.notifiedId = notifiedId
            this.notifiedToken = notifiedToken
            this.postPublisherId = postPublisherId
            this.postId = postId
            this.notificationType = notificationType
            this.firstCollectionType =firstCollectionType
            this.creatorReferenceId = creatorReferenceId
            this.secondCollectionType = secondCollectionType
            this.commentId = commentId
        }
    }

    fun buildNotificationHandlerForPostReacts(
         notifierId: String,
         notifierName: String,
         notifierImageUrl: String,
         notifiedId: String,
         notifiedToken: String,
         notificationType: String,
         postPublisherId: String,
         postId: String? = null,
         firstCollectionType: String,
         creatorReferenceId: String,
         secondCollectionType: String,

    ): NotificationsHandler{
        return notificationsHandler.apply{
            this.notifierId = notifierId
            this.notifierName = notifierName
            this.notifierImageUrl = notifierImageUrl
            this.notifiedId = notifiedId
            this.notifiedToken = notifiedToken
            this.postPublisherId = postPublisherId
            this.postId = postId
            this.notificationType = notificationType
            this.firstCollectionType =firstCollectionType
            this.creatorReferenceId = creatorReferenceId
            this.secondCollectionType = secondCollectionType
        }
    }

    fun buildNotificationHandlerForGroupJoinRequest(
        notifierId: String,
        notifierName: String,
        notifierImageUrl: String,
        notifiedId: String,
        notifiedToken: String,
        group: Group
        ): NotificationsHandler{
        return notificationsHandler.apply{
            this.notifierId = notifierId
            this.notifierName = notifierName
            this.notifierImageUrl = notifierImageUrl
            this.notifiedId = notifiedId
            this.notifiedToken = notifiedToken
            this.notificationType = "groupJoinRequest"
            this.groupId = group.id
            this.groupName = group.name
        }
    }

    fun buildNotificationHandlerForPostShares(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
    ): NotificationsHandler{
        return notificationsHandler.apply {
            this.notifierId = interactorId
            this.notifierName = interactorName
            this.notifierImageUrl = interactorImageUrl
            this.notifiedId = post.publisherId
            this.notifiedToken = notifiedToken
            this.postPublisherId = post.publisherId
            this.postId = post.id
            this.notificationType = "share"
            this.firstCollectionType =firstCollectionType
            this.creatorReferenceId = creatorReferenceId
            this.secondCollectionType = secondCollectionType
        }
    }

    fun buildNotificationHandlerForGroupPostCreation(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        notifiedToken: String,
    ): NotificationsHandler{
        return notificationsHandler.apply {
            this.notifierId = interactorId
            this.notifierName = interactorName
            this.notifierImageUrl = interactorImageUrl
//            this.notifiedId = post.publisherId
            this.notifiedToken = notifiedToken
            this.postPublisherId = post.publisherId
            this.postId = post.id
            this.groupName = post.groupName
            this.notificationType = "groupPost"
            this.firstCollectionType = post.firstCollectionType
            this.creatorReferenceId = post.creatorReferenceId
            this.secondCollectionType = post.secondCollectionType
        }
    }

    fun deleteReactFromPost(react: React, post: Post): Task<Void> {
        return postViewModel.deleteReactFromPost(react, post)
    }

    fun addShareToPost(share: Share, post: Post): Task<Void> {
        return postViewModel.addShareToPost(share, post)
    }

    fun showReactsChooserDialog(
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        modifiedPost: Post,
        currentReact: React?,
        notificationsHandler: NotificationsHandler?
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.long_clicked_reacts_button)

        val react = React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl
        )

        dialog.loveReactButton.setOnClickListener {
            react.react = 2
            notificationsHandler?.reactType = 2
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost, notificationsHandler)
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            notificationsHandler?.reactType = 3
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost, notificationsHandler)
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            notificationsHandler?.reactType = 4
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost, notificationsHandler)
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            notificationsHandler?.reactType = 5
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost, notificationsHandler)
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            notificationsHandler?.reactType = 6
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost, notificationsHandler)
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            notificationsHandler?.reactType = 7
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost, notificationsHandler)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun handleLongReactCreationAndDeletion(
        currentUserId: String,
        currentReact: React?,
        react: React,
        modifiedPost: Post,
        notificationsHandler: NotificationsHandler?
    ) {
        if (currentReact != null) {
            deleteReactFromPost(currentReact, modifiedPost)
        }

        addReactOnPostToDb(currentUserId, react, modifiedPost, notificationsHandler)
    }

    fun openCommentsBottomSheet(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        commentsBottomSheetListener: CommentsBottomSheetListener?
    ) {
        currentEditedPostPosition = postPosition
        val commentsBottomSheet = CommentsBottomSheet(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            commentsBottomSheetListener
        )
        commentsBottomSheet.show((context as AppCompatActivity).supportFragmentManager, commentsBottomSheet.tag)
    }

    fun openPostConfigurationsBottomSheet(post: Post, shared: Boolean?) {
        val postConfigurationsBottomSheet = PostConfigurationsBottomSheet(post, shared)
        postConfigurationsBottomSheet.show(
            (context as AppCompatActivity).supportFragmentManager,
            postConfigurationsBottomSheet.tag
        )
    }
}