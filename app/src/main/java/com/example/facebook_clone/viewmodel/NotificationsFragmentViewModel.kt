package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.livedata.UserLiveData
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Deferred

class NotificationsFragmentViewModel(private val usersRepository: UsersRepository): ViewModel() {

    fun getUserLiveData(userId: String): LiveData<User>? {
        return usersRepository.getUserLiveData(userId)
    }

    fun getNotificationsLiveData(userId: String): LiveData<List<Notification>> {
        return usersRepository.getNotificationsLiveData(userId)
    }

    fun deleteNotificationById(notifiedId: String,notificationId: String): Task<Void>{
       return usersRepository.deleteNotificationById(notifiedId, notificationId)
    }
}