package com.example.facebook_clone.ui.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.activity_video_player.*

private const val VIDEO_URL = "videoUrl"

class VideoPlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_video_player)


        val videoUrl = intent.getStringExtra(VIDEO_URL)

        videoView.setVideoPath(videoUrl)
        videoView.start()

        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)
        mediaController.setAnchorView(videoView)


    }
}