package com.example.facebook_clone.helper

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.dialog.ImageViewerDialog

class PostHandler(private val context: Context): PostListener {

    override fun onReactButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {

    }

    override fun onReactButtonLongClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {

    }

    override fun onCommentButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {

    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {

    }

    override fun onReactLayoutClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {

    }

    override fun onMediaPostClicked(mediaUrl: String) {
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show((context as AppCompatActivity).supportFragmentManager, "signature")
            imageViewerDialog.setMediaUrl(mediaUrl)
        } else {
            val videoIntent = Intent(context, VideoPlayerActivity::class.java)
            videoIntent.putExtra("videoUrl", mediaUrl)
            context.startActivity(videoIntent)
        }
    }

    override fun onPostMoreDotsClicked(post: Post, shared: Boolean?) {

    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {

    }


}