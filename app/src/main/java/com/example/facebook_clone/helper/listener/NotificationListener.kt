package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.notification.Notification

interface NotificationListener {
    fun onClickFriendRequestNotification(userId: String)
    fun onClickDeleteFriendRequestNotification(notificationId: String)
    fun onClickConfirmFriendRequestNotification(notificationId: String,userId:String, userName:String, userImageUrl:String)
}