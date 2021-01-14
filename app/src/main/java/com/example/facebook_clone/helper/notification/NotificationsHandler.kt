package com.example.facebook_clone.helper.notification

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.*

private const val TAG = "NotificationsHandler"
class NotificationsHandler(
    var notifierId: String? = null,
    var notifierName: String? = null,
    var notifierImageUrl: String? = null,
    var notifiedId: String? = null,
    var notifiedToken: String? = null,
    var notificationId: String? = null,
    var notificationType: String? = null,
    var postPublisherId: String? = null,
    var postId: String? = null,
    var groupName: String? = null,
    var reactType: Int? = null,
    var commentId: String? = null,
    var groupId: String? = null,
    var firstCollectionType: String = "",
    var creatorReferenceId: String = "",
    var secondCollectionType: String = "",
//    var post: Post,

    private val othersProfileActivityViewModel: OthersProfileActivityViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel
) : AppCompatActivity(){


    //[1]
   private fun createNotification(): Notification{
        return Notification(
            notificationType = notificationType,
            notifierId = notifierId,
            notifiedId = notifiedId,
            notifierName = notifierName,
            notifierImageUrl = notifierImageUrl,
            postId = postId,
            postPublisherId = postPublisherId,
            groupName = groupName,
            groupId = groupId,
            reactType = reactType,
            firstCollectionType = firstCollectionType,
            creatorReferenceId = creatorReferenceId,
            secondCollectionType = secondCollectionType,
            commentId = commentId
        )
    }

    //[2]
    private fun addNotificationIdToNotifiedDocument(){
        val notification = createNotification()
        Log.d(TAG, "TTTT addNotificationIdToNotifiedDocument: $notification")
        addNotificationToNotificationCollection(notification).addOnCompleteListener { task ->
            if (task.isSuccessful){
                fireServerSideNotification(notification)
                othersProfileActivityViewModel
                    .addNotificationIdToNotifiedDocument(notification.id.toString(), notifiedId!!)
                    .addOnCompleteListener {task2 ->
                        if (task2.isSuccessful){
                            //Notify user
//                            fireAnyNotification(notification)
                        }
                    }
            }
        }

    }

    //[3]
    private fun addNotificationToNotificationCollection(notification: Notification): Task<Void>{
        return othersProfileActivityViewModel.addNotificationToNotificationsCollection(notification, notifiedId!!)
    }

    fun handleNotificationCreationAndFiring(){
        addNotificationIdToNotifiedDocument()
    }

    //[4]
    fun deleteNotificationFromNotificationsCollection(){
        notificationsFragmentViewModel.deleteNotificationById(notifiedId!!, notificationId!!)
    }

    //[5]
    fun deleteNotificationIdFromNotifiedDocument(){
        othersProfileActivityViewModel.deleteNotificationIdFromNotifiedDocument(notificationId!!, notifiedId!!)
    }


    private fun fireServerSideNotification(notification: Notification) = CoroutineScope(
        Dispatchers.IO).launch {
        try {
            val pushNotification = PushNotification(
                data = notification,
                to = notifiedToken
            )
            val response = RetrofitInstance.api.postNotification(pushNotification)
            if(response.isSuccessful) {
                Log.i(TAG, "GOGOG Response: Success")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }



}