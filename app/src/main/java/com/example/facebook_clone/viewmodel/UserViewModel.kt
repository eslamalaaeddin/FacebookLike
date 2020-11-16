package com.example.facebook_clone.viewmodel

import android.app.Notification
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask

class UserViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    fun getMe(userId: String): LiveData<User>? {
        return usersRepository.getUser(userId)
    }

    fun getAnotherUser(userId: String): LiveData<User>? {
        return usersRepository.getUser(userId)
    }

    fun uploadProfileImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.uploadImageToCloudStorage(bitmap,"profile")
    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.uploadProfileImageToUserCollection(photoUrl)
    }

    fun uploadCoverImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.uploadImageToCloudStorage(bitmap,"cover")
    }

    fun uploadCoverImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.uploadCoverImageToUserCollection(photoUrl)
    }

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


}