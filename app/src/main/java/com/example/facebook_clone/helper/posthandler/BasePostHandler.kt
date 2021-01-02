package com.example.facebook_clone.helper.posthandler

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.BaseApplication.Companion.context
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.PostConfigurationsBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*

open class BasePostHandler(
    private val context: Context,
    private val postViewModel: PostViewModel
) {
    var progressDialog: ProgressDialog? = null
    var currentEditedPostPosition: Int = -1

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

    fun addReactOnPostToDb(currentUserId: String, react: React, post: Post) {
        postViewModel.addReactToDB(react, post).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (currentUserId != post.publisherId) {
                    Toast.makeText(context, "Notify Him", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
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
        currentReact: React?
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
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost)
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost)
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost)
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost)
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost)
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            handleLongReactCreationAndDeletion(interactorId, currentReact, react, modifiedPost)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun handleLongReactCreationAndDeletion(
        currentUserId: String,
        currentReact: React?,
        react: React,
        modifiedPost: Post
    ) {
        if (currentReact != null) {
            deleteReactFromPost(currentReact, modifiedPost)
        }
        addReactOnPostToDb(currentUserId, react, modifiedPost)
    }

    fun openCommentsBottomSheet(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
        val commentsBottomSheet = CommentsBottomSheet(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            null,
            "",
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