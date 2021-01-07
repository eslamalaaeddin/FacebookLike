package com.example.facebook_clone.helper.notification

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.facebook_clone.helper.BaseApplication
import com.example.facebook_clone.model.notification.Notifier
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
        val notifiedId = message.data["notifiedId"]
        val notifierImageUrl = message.data["notifierImageUrl"]
        val postId = message.data["postId"]
        val postPublisherId= message.data["postPublisherId"]
        val commentPosition = message.data["commentPosition"]?.toInt()
        val firstCollectionType = message.data["firstCollectionType"]
        val secondCollectionType = message.data["secondCollectionType"]
        val creatorReferenceId = message.data["creatorReferenceId"]

        Log.i(TAG, "FAWZY onMessageReceived: $message")
        Log.i(TAG, "FAWZY onMessageReceived: ${message.data}")
        Log.i(TAG, "FAWZY onMessageReceived: $notificationType")
        Log.i(TAG, "FAWZY onMessageReceived: $notifierName")
        Log.i(TAG, "FAWZY onMessageReceived: $notifierId")
        Log.i(TAG, "FAWZY onMessageReceived: $notifierImageUrl")


        createClientSideNotification(notificationType,
            notifierName,
            notifierId,
            notifierImageUrl,
            postId = postId,
            postPublisherId = postPublisherId,
            commentPosition = commentPosition,
            notifiedId = notifiedId
        )
    }

    private fun createClientSideNotification(
        notificationType: String?,
        notifierName: String?,
        notifierId: String?,
        notifierImageUrl: String?,
        notifiedId: String?,
        postId: String?,
        postPublisherId:String?,
        commentPosition: Int?,
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
                        BaseApplication.fireClientSideNotification(
                            notificationType!!,
                            notifier,
                            postId,
                            postPublisherId ,
                            commentPosition,
                            notifiedId)
                    }
                })
        }
    }
}











