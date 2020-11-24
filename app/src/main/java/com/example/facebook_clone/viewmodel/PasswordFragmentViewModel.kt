package com.example.facebook_clone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


class PasswordFragmentViewModel(private val auth: FirebaseAuth, private val usersRepository: UsersRepository) : ViewModel() {

    fun createAccountWithMailAndPassword(email: String, password: String) : Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun createRecentUsersCollection(token: String, recentLoggedInUser: RecentLoggedInUser): Task<Void>{
        return usersRepository.createRecentUsersCollection(token, recentLoggedInUser)
    }

    fun uploadUserDataToDB(user: User) : Task<Void>{
        return usersRepository.addUserNewAccountData(user)
    }

}