package com.example.facebook_clone

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.facebook_clone.helper.BaseApplication
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.notification.Notifier
import com.example.facebook_clone.ui.activity.MainActivity
import kotlinx.android.synthetic.main.activity_testing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val IMAGE_REQUEST_CODE = 159
private const val TAG = "TestingActivity"

class TestingActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var notification: Notification
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)
        val imageUrl = "https://firebasestorage.googleapis.com/v0/b/facebook-clone-5e8ed.appspot.com/o/xVfyUJlnN9RQpBwvN5KZuYdQKcl2%2FProfile%20images%2Fprofile%2FxVfyUJlnN9RQpBwvN5KZuYdQKcl2.jpeg?alt=media&token=2aa3c4d4-9bff-408f-b996-dd13551d6490"

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .circleCrop()
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmap = resource
                    val notifier = Notifier(
                        id = "xVfyUJlnN9RQpBwvN5KZuYdQKcl2",
                        bitmap,
                        name = "Islam AlaaEddin"
                    )
                     notification = Notification("comment", notifier)
                }
            })

        button.setOnClickListener {
            BaseApplication.fireNotification(notification, MainActivity::class.java)
        }
    }

}