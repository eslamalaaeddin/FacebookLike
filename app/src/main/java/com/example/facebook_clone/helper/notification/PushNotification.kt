package com.example.facebook_clone.helper.notification

import com.example.facebook_clone.helper.notification.NotificationData
import com.example.facebook_clone.model.notification.Notification

data class PushNotification(
    val data: Notification,
    var to: String? = null
)