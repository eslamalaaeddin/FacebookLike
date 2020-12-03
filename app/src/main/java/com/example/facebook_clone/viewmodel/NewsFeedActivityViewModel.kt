package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.SetOptions

class NewsFeedActivityViewModel(private val usersRepository: UsersRepository): ViewModel() {

    fun getMe(userId: String): LiveData<User>? {
        return usersRepository.getUserLiveData(userId)
    }



    fun addUserToRecentLoggedInUsers(recentUser: RecentLoggedInUser, token: String): Task<Void> {
        return usersRepository.addUserToRecentLoggedInUsers(recentUser, token)
    }

    fun createRecentUsersCollection(token: String, recentUser: RecentLoggedInUser): Task<Void>{
        return usersRepository.createRecentUsersCollection(token, recentUser)
    }

    fun updateUserToken(token: String, userId: String): Task<Void>{
        return usersRepository.updateUserToken(token, userId)
    }

    fun deleteUserFromRecentLoggedInUsers(recentUser: RecentLoggedInUser, token: String): Task<Void> {
        return usersRepository.deleteUserFromRecentLoggedInUsers(recentUser, token)
    }
}