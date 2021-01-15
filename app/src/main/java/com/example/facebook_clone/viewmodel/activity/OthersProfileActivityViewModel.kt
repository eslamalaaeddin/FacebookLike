package com.example.facebook_clone.viewmodel.activity

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.user.followed.Followed
import com.example.facebook_clone.model.user.follower.Follower
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue

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


    fun deleteFriendFromFriends(friend: Friend, userId: String): Task<Void>{
        return usersRepository.deleteFriendFromFriends(friend, userId)
    }

    fun addMeAsAFollowerToHisDocument(followedId: String, follower: Follower): Task<Void>{
        return usersRepository.addMeAsAFollowerToHisDocument(followedId, follower)
    }

    fun deleteMeAsAFollowerFromHisDocument(followedId: String, follower: Follower): Task<Void>{
        return usersRepository.deleteMeAsAFollowerFromHisDocument(followedId, follower)
    }

    fun addHimAsAFollowingToMyDocument(myId: String, followed: Followed): Task<Void>{
        return usersRepository.addHimAsAFollowingToMyDocument(myId, followed)
    }

    fun deleteHimAsAFollowingFromMyDocument(myId: String, followed: Followed): Task<Void>{
        return usersRepository.deleteHimAsAFollowingFromMyDocument(myId, followed)
    }


}