package com.example.facebook_clone.model.notification

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.*

data class Notification(
    var notificationType: String? = null,
    var notifier: Notifier? = null,
    val id: String? = UUID.randomUUID().toString(),
    val notificationTime: Timestamp = Timestamp(Date())
)
