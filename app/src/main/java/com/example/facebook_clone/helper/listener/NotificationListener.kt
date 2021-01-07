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
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    )

    fun onClickShareOnPostNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    )

    fun onClickCommentOnPostNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentPosition: Int
    )

    fun onClickReactsOnCommentNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentPosition: Int
    )

    fun onNotificationLongClicked(notification: Notification)

    fun onClickOnGroupInvitationNotification(notifierId: String, groupId: String)

    fun onAcceptGroupInvitationButtonClicked(groupId: String, notificationId: String)
    fun onCancelGroupInvitationButtonClicked(notifiedId: String, notificationId: String)


}