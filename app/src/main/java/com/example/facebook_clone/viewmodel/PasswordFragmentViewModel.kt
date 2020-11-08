package com.example.facebook_clone.viewmodel

import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Util
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class PasswordFragmentViewModel(private val auth: FirebaseAuth) : ViewModel() {



    fun createAccountWithMailAndPassword(email: String, password: String) : Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }
//
//    fun createAccountWithPhoneNumberAndPassword(credential : PhoneAuthCredential, phone : String, password: String) {
//        auth.signInWithCredential(credential)
//
//    }

}