package com.example.facebook_clone.helper.notification

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.facebook_clone.helper.BaseApplication
import com.example.facebook_clone.model.notification.Notifier
import com.example.facebook_clone.ui.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "my_channel"
private const val TAG = "FirebaseService"
class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationType = message.data["notificationType"]
        val notifierName = message.data["notifierName"]
        val notifierId = message.data["notifierId"]
        val notifierImageUrl = message.data["notifierImageUrl"]

        Log.i(TAG, "FAWZY onMessageReceived: $notificationType")
        Log.i(TAG, "FAWZY onMessageReceived: $notifierName")
        Log.i(TAG, "FAWZY onMessageReceived: $notifierId")
        Log.i(TAG, "FAWZY onMessageReceived: $notifierImageUrl")

        createNotification(notificationType, notifierName, notifierId, notifierImageUrl)
    }

    private fun createNotification(
        notificationType: String?,
        notifierName: String?,
        notifierId: String?,
        notifierImageUrl: String?
    ){

        CoroutineScope(Dispatchers.IO).launch {
            var bitmap: Bitmap? = null
            Glide.with(this@FirebaseService)
                .asBitmap()
                .load(notifierImageUrl)
                .circleCrop()
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bitmap = resource
                        val notifier = Notifier(
                            id = notifierId,
                            name = notifierName,
                            imageUrl = notifierImageUrl,
                            imageBitmap = bitmap
                        )
                        BaseApplication.fireNotification(notificationType!!, notifier)
                    }
                })
        }
    }
}











