package com.example.facebook_clone.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask

class ProfilePictureActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    fun uploadImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.uploadImageToCloudStorage(bitmap,"profile")
    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.uploadProfileImageToUserCollection(photoUrl)
    }
}