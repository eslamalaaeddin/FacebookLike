package com.example.facebook_clone.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask

class ProfilePictureActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    fun uploadProfileImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.uploadImageToCloudStorage(bitmap,"profile")
    }

    fun uploadCoverImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.uploadCoverImageToCloudStorage(bitmap)
    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.uploadProfileImageToUserCollection(photoUrl)
    }

    fun uploadCoverImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.uploadCoverImageToUserCollection(photoUrl)
    }
}