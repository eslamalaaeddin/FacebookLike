package com.example.facebook_clone.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask

class ProfileActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {
    fun getMe(userId: String): LiveData<User>? {
        return usersRepository.getUserLiveData(userId)
    }

    fun getAnotherUser(userId: String): LiveData<User>? {
        return usersRepository.getUserLiveData(userId)
    }


//    fun addUserToRecentLoggedInUsers(recentUser: RecentLoggedInUser): Task<Void>{
//        return usersRepository.addUserToRecentLoggedInUsers(recentUser)
//    }
//
//    fun deleteUserFromRecentLoggedInUsers(recentUser: RecentLoggedInUser): Task<Void>{
//        return usersRepository.deleteUserFromRecentLoggedInUsers(recentUser)
//    }

//    fun getNotifications(userId: String) : LiveData<List<Post>> {
//        return usersRepository.getNotificationsLiveData(userId)
//    }

    fun uploadProfileImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.addImageToCloudStorage(bitmap,"profile")
    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.addProfileImageToUserCollection(photoUrl)
    }

    fun uploadCoverImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.addImageToCloudStorage(bitmap,"cover")
    }

    fun uploadCoverImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.addCoverImageToUserCollection(photoUrl)
    }


}