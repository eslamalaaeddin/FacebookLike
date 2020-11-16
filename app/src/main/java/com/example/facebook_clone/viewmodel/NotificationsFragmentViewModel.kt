package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task

class NotificationsFragmentViewModel(private val usersRepository: UsersRepository): ViewModel() {

    fun getNotificationsLiveData(userId: String): LiveData<List<Notification>> {
        return usersRepository.getNotificationsLiveData(userId)
    }

    fun deleteNotificationById(notificationId: String): Task<Void>{
       return usersRepository.deleteNotificationById(notificationId)
    }
}