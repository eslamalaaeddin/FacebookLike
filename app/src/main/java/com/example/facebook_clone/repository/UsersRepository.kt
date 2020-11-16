package com.example.facebook_clone.repository

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.facebook_clone.helper.Utils.USERS_COLLECTION
import com.example.facebook_clone.livedata.UserLiveData
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream

private const val TAG = "UsersRepository"

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

    fun uploadImageToCloudStorage(bitmap: Bitmap, profileOrCover: String): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Profile images").child(profileOrCover)
                .child("${userId}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }

    fun uploadProfileImageToUserCollection(photoUrl: String): Task<Void> {
        val userId = auth.currentUser?.uid.toString()
        return database.collection(USERS_COLLECTION).document(userId)
            .update("profileImageUrl", photoUrl)
    }

    fun uploadCoverImageToUserCollection(photoUrl: String): Task<Void> {
        val userId = auth.currentUser?.uid.toString()
        return database.collection(USERS_COLLECTION).document(userId)
            .update("coverImageUrl", photoUrl)
    }

    fun searchForUsers(query: String): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        database.collection(USERS_COLLECTION)
            .addSnapshotListener { usersSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    liveData.postValue(
                        usersSnapshot?.toObjects(User::class.java)?.filterIndexed { _, user ->
                            user.name?.contains(query.toLowerCase())!!
                                    || user.name?.contains(query.toUpperCase())!!
                                    || user.name?.contains(query)!!
                        }
                    )
                }
            }

        return liveData
    }

    fun addFriendRequestToMyDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.fromId.toString())
            .update("friendRequests", FieldValue.arrayUnion(friendRequest))
    }

    fun addFriendRequestToHisDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.toId.toString())
            .update("friendRequests", FieldValue.arrayUnion(friendRequest))
    }

    fun removeFriendRequestFromMyDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.fromId.toString())
            .update("friendRequests", FieldValue.arrayRemove(friendRequest))
    }

    fun removeFriendRequestFromHisDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.toId.toString())
            .update("friendRequests", FieldValue.arrayRemove(friendRequest))
    }



}