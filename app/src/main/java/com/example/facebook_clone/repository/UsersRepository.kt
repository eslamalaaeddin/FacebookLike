package com.example.facebook_clone.repository

import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.example.facebook_clone.helper.Utils.USERS_COLLECTION
import com.example.facebook_clone.livedata.UserLiveData
import com.example.facebook_clone.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream

class UsersRepository(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    fun uploadUserDataToDB(user: User): Task<Void> {
        return database.collection(USERS_COLLECTION).document(user.id.toString()).set(user)
    }

    fun getUser(userId: String): LiveData<User>? {
        val documentReference = database.collection(USERS_COLLECTION).document(userId)
        return UserLiveData(documentReference)
    }

    fun uploadImageToCloudStorage(bitmap: Bitmap, profileOrCover:String): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child("Profile images").child(profileOrCover).child("${userId}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        val userId = auth.currentUser?.uid.toString()
        return database.collection(USERS_COLLECTION).document(userId).update("profileImageUrl",photoUrl)
    }

    fun uploadCoverImageToUserCollection(photoUrl: String): Task<Void> {
        val userId = auth.currentUser?.uid.toString()
        return database.collection(USERS_COLLECTION).document(userId).update("coverImageUrl",photoUrl)
    }


}