package com.example.facebook_clone.helper.posthandler

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.helper.BaseApplication.Companion.context
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.dialog.ImageViewerDialog

open class BasePostHandler (private val context: Context) {
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

    fun handleMediaClicks(mediaUrl: String){
        //Image
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show((context as AppCompatActivity).supportFragmentManager, "signature")
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
}