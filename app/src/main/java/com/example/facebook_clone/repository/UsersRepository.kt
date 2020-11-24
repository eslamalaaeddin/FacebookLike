package com.example.facebook_clone.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.Utils.MY_NOTIFICATIONS_COLLECTION
import com.example.facebook_clone.helper.Utils.RECENT_USERS_COLLECTION
import com.example.facebook_clone.helper.Utils.USERS_COLLECTION
import com.example.facebook_clone.livedata.UserLiveData
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.model.user.search.Search
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream

private const val TAG = "UsersRepository"

class UsersRepository(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    fun addUserNewAccountData(user: User): Task<Void> {
        return database.collection(USERS_COLLECTION).document(user.id.toString()).set(user)
    }



    fun addSearchToRecentSearches(search: Search, searcherId: String): Task<Void>{
        return database.collection(USERS_COLLECTION).document(searcherId)
            .update("searches", FieldValue.arrayUnion(search))
    }

    fun deleteSearchFromRecentSearches(search: Search, searcherId: String): Task<Void>{
        return database.collection(USERS_COLLECTION).document(searcherId)
            .update("searches", FieldValue.arrayRemove(search))
    }

    fun createRecentUsersCollection(token: String, recentUser: RecentLoggedInUser): Task<Void>{
        return database.collection(RECENT_USERS_COLLECTION).document(token).set(hashMapOf("recentUsers" to recentUser), SetOptions.mergeFields())
    }

    fun addUserToRecentLoggedInUsers(recentUser: RecentLoggedInUser, token: String): Task<Void>{
        return database.collection(RECENT_USERS_COLLECTION).document(token)
            .update("recentUsers", FieldValue.arrayUnion(recentUser))
    }

    fun getRecentLoggedInUsers(token: String): DocumentReference{
        return database.collection(RECENT_USERS_COLLECTION).document(token)
    }


    fun deleteUserFromRecentLoggedInUsers(recentUser: RecentLoggedInUser, token: String): Task<Void>{
        return database.collection(USERS_COLLECTION).document(token)
            .update("recentUsers", FieldValue.arrayRemove(recentUser))
    }

    fun addHimToMyFriendsAndMeToHisFriends(meAsFriend: Friend, himAsFriend: Friend): Task<Void>{

        return database.collection(USERS_COLLECTION).document(meAsFriend.id.toString()).
        update("friends", FieldValue.arrayUnion(himAsFriend)).addOnCompleteListener {
            if (it.isSuccessful){
                database.collection(USERS_COLLECTION).document(himAsFriend.id.toString()).
                update("friends", FieldValue.arrayUnion(meAsFriend))
            }
        }
    }

    //AddingImages
    fun addImageToCloudStorage(bitmap: Bitmap, profileOrCover: String): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Profile images").child(profileOrCover)
                .child("${userId}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }
    fun addCoverImageToCloudStorage(bitmap: Bitmap): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Profile images").child("cover")
                .child("${userId}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }
    fun addProfileImageToUserCollection(photoUrl: String): Task<Void> {
        val userId = auth.currentUser?.uid.toString()
        return database.collection(USERS_COLLECTION).document(userId)
            .update("profileImageUrl", photoUrl)
    }
    fun addCoverImageToUserCollection(photoUrl: String): Task<Void> {
        val userId = auth.currentUser?.uid.toString()
        return database.collection(USERS_COLLECTION).document(userId)
            .update("coverImageUrl", photoUrl)
    }

    //AddingFriendRequests
    fun addFriendRequestToMyDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.fromId.toString())
            .update("friendRequests", FieldValue.arrayUnion(friendRequest))
    }
    fun addFriendRequestToHisDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.toId.toString())
            .update("friendRequests", FieldValue.arrayUnion(friendRequest))
    }

    //AddingNotifications
    fun addNotificationToNotificationsCollection(notification: Notification, userToBeNotifiedId: String): Task<Void>{
        return database.collection(Utils.NOTIFICATIONS_COLLECTION).document(userToBeNotifiedId)
            .collection(Utils.MY_NOTIFICATIONS_COLLECTION).document(notification.id.toString()).set(notification)
    }
    fun addNotificationIdToHisDocument(notificationId: String, hisId: String): Task<Void>{
        return database.collection(USERS_COLLECTION).document(hisId)
            .update("notificationsIds", FieldValue.arrayUnion(notificationId))
    }

    //GettingUsers
    fun getUserLiveData(userId: String): LiveData<User>? {
        val documentReference = database.collection(USERS_COLLECTION).document(userId)
        return UserLiveData(documentReference)
    }


    fun getUserAsRegularObjectNotLiveData(userId: String): Task<DocumentSnapshot> {
        return database.collection(USERS_COLLECTION).document(userId).get()
    }
    fun getUsersLiveDataAfterSearchingByName(query: String): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        database.collection(USERS_COLLECTION)
            .addSnapshotListener { usersSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    liveData.postValue(
                        usersSnapshot?.toObjects(User::class.java)?.filterIndexed { _, user ->
                            Log.i(TAG, "JJJJ getUsersLiveDataAfterSearchingByName: $user")
                            user.name?.contains(query.toLowerCase())!!
                                    || user.name?.contains(query.toUpperCase())!!
                                    || user.name?.contains(query)!!
                        }
                    )
                }
            }

        return liveData
    }

    //DeletingFriendRequests
    fun deleteFriendRequestFromMyDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.fromId.toString())
            .update("friendRequests", FieldValue.arrayRemove(friendRequest))
    }
    fun deleteFriendRequestFromHisDocument(friendRequest: FriendRequest): Task<Void> {
        return database.collection(USERS_COLLECTION).document(friendRequest.toId.toString())
            .update("friendRequests", FieldValue.arrayRemove(friendRequest))
    }
    fun deleteNotificationIdFromNotifiedDocument(notificationId: String, hisId: String): Task<Void>{
        return database.collection(USERS_COLLECTION).document(hisId)
            .update("notificationsIds", FieldValue.arrayRemove(notificationId))
    }
    fun deleteNotificationById(notifiedId: String,notificationId: String): Task<Void>{
        return  database
            .collection(Utils.NOTIFICATIONS_COLLECTION)
            .document(notifiedId)
            .collection(MY_NOTIFICATIONS_COLLECTION)
            .document(notificationId).delete()
    }

    //GettingNotifications
    /*
        this function is called in OthersProfile. to delete friend request notification from other user
        if i canceled the request
     */
    fun getNotificationsLiveData(userId: String): LiveData<List<Notification>>{
        var notifications: MutableList<Notification>?
        val liveData = MutableLiveData<List<Notification>>()
        database.collection(Utils.NOTIFICATIONS_COLLECTION).document(userId)
            .collection(Utils.MY_NOTIFICATIONS_COLLECTION)
            .orderBy("notificationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { notificationsSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    notifications = notificationsSnapshot?.toObjects(Notification::class.java)
                    liveData.postValue(notifications)

                }
            }
        return liveData
    }

}