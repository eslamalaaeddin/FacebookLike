package com.example.facebook_clone.model.notification

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.*

data class Notification(
    var postPublisherId: String? = null,
    var notificationType: String? = null,
    var notifierId: String? = null,
    var notifiedId: String? = null,//it is used to delete notificatoin
    @get:Exclude var imageBitmap: Bitmap? = null,
    var notifierName: String? = null,
    var notifierImageUrl: String? = null,
    var postId: String? = null,
    var reactType: Int? = 1,
    var commentPosition: Int? = null,
    val id: String? = UUID.randomUUID().toString(),
    val notificationTime: Timestamp = Timestamp(Date())
)
