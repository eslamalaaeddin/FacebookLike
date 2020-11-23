package com.example.facebook_clone

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.facebook_clone.helper.BaseApplication
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.notification.NotificationData
import com.example.facebook_clone.helper.notification.PushNotification
import com.example.facebook_clone.helper.notification.RetrofitInstance
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.notification.Notifier
import com.example.facebook_clone.ui.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_testing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TestingActivity"
class TestingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        profile.setOnClickListener {
            val defaultProfileImageBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.hairstyle
            )
            Log.i(TAG, "onCreate: $defaultProfileImageBitmap")
            imageView.setImageBitmap(defaultProfileImageBitmap)
        }

        cover.setOnClickListener {
            val defaultCoverImageBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.niqab
            )
            Log.i(TAG, "onCreate: $defaultCoverImageBitmap")
            imageView.setImageBitmap(defaultCoverImageBitmap)
        }
    }

}