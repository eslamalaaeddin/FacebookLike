package com.example.facebook_clone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task

class OthersProfileActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    fun addFriendRequestToMyDocument(friendRequest: FriendRequest): Task<Void> {
        return usersRepository.addFriendRequestToMyDocument(friendRequest)
    }

    fun addFriendRequestToHisDocument(friendRequest: FriendRequest): Task<Void> {
        return usersRepository.addFriendRequestToHisDocument(friendRequest)
    }

    fun removeFriendRequestFromMyDocument(friendRequest: FriendRequest): Task<Void> {
        return usersRepository.deleteFriendRequestFromMyDocument(friendRequest)
    }

    fun removeFriendRequestFromHisDocument(friendRequest: FriendRequest): Task<Void> {
        return usersRepository.deleteFriendRequestFromHisDocument(friendRequest)
    }

    fun addNotificationToNotificationsCollection(notification: Notification, notifiedId: String): Task<Void>{
        return usersRepository.addNotificationToNotificationsCollection(notification, notifiedId)
    }

    fun addNotificationIdToNotifiedDocument(notificationId: String, notifiedId: String): Task<Void>{
         return usersRepository.addNotificationIdToHisDocument(notificationId, notifiedId)
    }

    fun deleteNotificationIdFromNotifiedDocument(notificationId: String, notifiedId: String): Task<Void>{
        return usersRepository.deleteNotificationIdFromNotifiedDocument(notificationId, notifiedId)
    }

    fun createFriendshipBetweenMeAndHim(meAsFriend: Friend, himAsFriend: Friend): Task<Void>{
        return usersRepository.addHimToMyFriendsAndMeToHisFriends(meAsFriend, himAsFriend)
    }


}