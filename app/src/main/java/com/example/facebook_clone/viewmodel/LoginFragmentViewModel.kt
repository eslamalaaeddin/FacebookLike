package com.example.facebook_clone.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginFragmentViewModel(private val auth: FirebaseAuth) : ViewModel() {
    fun signIn(email: String, password: String) : Task<AuthResult>{
       return auth.signInWithEmailAndPassword(email, password)
    }
}