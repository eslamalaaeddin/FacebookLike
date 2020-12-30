//package com.example.facebook_clone.helper
//
//import android.content.Context
//import android.content.Intent
//import androidx.core.content.ContextCompat.startActivity
//import com.example.facebook_clone.ui.activity.VideoPlayerActivity
//import com.example.facebook_clone.ui.dialog.ImageViewerDialog
//
//class MediaHandler(private val context: Context) {
//    fun handleMedia(mediaUrl: String){
//        if (mediaUrl.contains("jpeg")) {
//            val imageViewerDialog = ImageViewerDialog()
//            imageViewerDialog.show(supportFragmentManager, "signature")
//            imageViewerDialog.setMediaUrl(mediaUrl)
//        } else {
//            val videoIntent = Intent(context, VideoPlayerActivity::class.java)
//            videoIntent.putExtra("videoUrl", mediaUrl)
//            context.startActivity(videoIntent)
//        }
//    }
//
//}