package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.notification.Notification
import java.util.concurrent.Flow

interface NotificationListener {
    fun onClickFriendRequestNotification(userId: String)
    fun onClickDeleteFriendRequestNotification(notifiedId: String, notificationId: String)
    fun onClickConfirmFriendRequestNotification(notifiedId: String, notificationId: String,userId:String, userName:String, userImageUrl:String)

    fun onClickReactOnPostNotification(
        postPublisherId: String,
        postId: String,
    )

    fun onClickCommentOnPostNotification(
        postPublisherId: String,
        postId: String,
        commentPosition: Int
    )


}