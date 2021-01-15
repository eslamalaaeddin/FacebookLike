package com.example.facebook_clone.viewmodel.activity

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

class RecentUsersActivityViewModel(private val usersRepository: UsersRepository): ViewModel() {
    fun getRecentLoggedInUsers(token: String): DocumentReference {
        return usersRepository.getRecentLoggedInUsers(token)
    }
}