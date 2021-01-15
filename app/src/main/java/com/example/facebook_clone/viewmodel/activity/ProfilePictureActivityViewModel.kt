package com.example.facebook_clone.viewmodel.activity

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.repository.UsersRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask

class ProfilePictureActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {

    fun uploadProfileImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.addImageToCloudStorage(bitmap,"profile")
    }

    fun uploadCoverImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return usersRepository.addCoverImageToCloudStorage(bitmap)
    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.addProfileImageToUserCollection(photoUrl)
    }

    fun uploadCoverImageToUserCollection(photoUrl: String): Task<Void> {
        return usersRepository.addCoverImageToUserCollection(photoUrl)
    }
}