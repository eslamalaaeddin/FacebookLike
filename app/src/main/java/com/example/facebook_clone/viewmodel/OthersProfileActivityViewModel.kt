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
        return usersRepository.removeFriendRequestFromMyDocument(friendRequest)
    }

    fun removeFriendRequestFromHisDocument(friendRequest: FriendRequest): Task<Void> {
        return usersRepository.removeFriendRequestFromHisDocument(friendRequest)
    }

    fun addNotificationToNotificationsCollection(notification: Notification, userToBeNotifiedId: String): Task<Void>{
        return usersRepository.addNotificationToNotificationsCollection(notification, userToBeNotifiedId)
    }

    fun createFriendshipBetweenMeAndHim(meAsFriend: Friend, himAsFriend: Friend): Task<Void>{
        return usersRepository.createFriendshipBetweenMeAndHim(meAsFriend, himAsFriend)
    }


}