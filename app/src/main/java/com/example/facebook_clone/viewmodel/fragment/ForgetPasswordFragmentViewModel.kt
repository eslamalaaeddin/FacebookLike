package com.example.facebook_clone.viewmodel.fragment

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordFragmentViewModel(private val auth: FirebaseAuth) : ViewModel() {

    fun sendEmailToResetPassword(email:String): Task<Void>{
        return auth.sendPasswordResetEmail(email)
    }
}