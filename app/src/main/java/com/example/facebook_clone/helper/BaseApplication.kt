package com.example.facebook_clone.helper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import com.example.facebook_clone.R
import com.example.facebook_clone.di.*
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.notification.Notifier
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.repository.UsersRepository
import com.example.facebook_clone.ui.activity.MainActivity
import com.example.facebook_clone.ui.activity.OthersProfileActivity
import com.example.facebook_clone.ui.activity.PostViewerActivity
import com.example.facebook_clone.ui.activity.ProfileActivity
import com.google.api.Billing
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.*
import kotlin.random.Random

private const val CHANNEL_ID = "123"
private const val CHANNEL_NAME = "channel name"
private const val NOTIFICATION_ID = 123
private const val NOTIFICATION_TITLE = "My Notification"
private const val NOTIFICATION_CONTENT = "My Notification Content"

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(applicationContext)
        context = this
        createNotificationChannel()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    databaseModule,
                    firebaseAuthModule,
                    firebaseStorageModule,
                    passFragViewModelModule,
                    loginFragViewModelModule,
                    forgetPasswordFragViewModelModule,
                    usersRepositoryModule,
                    profileActivityViewModelModule,
                    profilePictureActivityViewModelModule,
                    postsRepositoryModule,
                    postCreatorViewModelModule,
                    searchActivityViewModelModule,
                    othersProfileActivityViewModelModule,
                    notificationsFragmentViewModelModule,
                    newsFeedActivityViewModelModule,
                    recentUsersActivityViewModelModule
                )
            )
        }



//        getCurrentUserOnlyOnce()

    }

    private fun createNotificationChannel() {
        //1 Create the channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context?.getString(R.string.channel_description)

            val notificationManager: NotificationManager? =
                context?.getSystemService(NotificationManager::class.java)

            notificationManager?.createNotificationChannel(channel)
        }
    }


    //Notification provider
    companion object {
        var singletonUser: User? = null
        var context: Context? = null
        var destination: Class<*>? = null
        //val usersRepository = UsersRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance(), FirebaseStorage.getInstance())
        fun fireNotification(
            notificationType: String,
            notifier: Notifier,
            postId: String?,
            commentPosition: Int?,
            notifiedId: String?
        ) {

            val remoteView = RemoteViews(context?.packageName, R.layout.custom_notification_layout)

            ////////////////////// CUSTOMIZING THE NOTIFICATION //////////////////////////////
            when (notificationType) {

                "friendRequest" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} sent you a friend request"
                    )
                    destination = OthersProfileActivity::class.java
                }


                "commentOnPost" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} commented on your post"
                    )
                    //temp
                    destination = PostViewerActivity::class.java
                }


                "reactOnPost" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} reacted to your post"
                    )

                    destination = PostViewerActivity::class.java
                }

                "share" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} shared your post"
                    )
                    destination = PostViewerActivity::class.java
                }

                "reactOnComment" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} reacted to your Comment"
                    )

                    destination = PostViewerActivity::class.java
                }

                "groupPost" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} sent you a friend request"
                    )
                }

                "acceptedInGroup" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} sent you a friend request"
                    )
                }

            }

            ///////////////////////////////////////////////////////////////////////////////
            remoteView.setImageViewBitmap(R.id.notificationImageView, notifier.imageBitmap)

            val sound: Uri =
                Uri.parse("android.resource://" + context?.packageName + "/" + R.raw.facebook_notification_sound)
            //2 Create the builder
            val builder = NotificationCompat.Builder(context!!, CHANNEL_NAME)
                .setSmallIcon(R.drawable.facebook)
//                .setContentTitle(NOTIFICATION_TITLE)
//                .setContentText(NOTIFICATION_CONTENT)
                .setCustomContentView(remoteView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setChannelId(CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(sound)
                .setVibrate(longArrayOf(0, 250, 100, 250))
                .setAutoCancel(true)
            //3 Create the action
            val actionIntent = Intent(context, destination)
            actionIntent.putExtra("friendRequester", notifier.id.toString())
            actionIntent.putExtra("postPublisherId", notifiedId)
            actionIntent.putExtra("postId", postId)
            actionIntent.putExtra("commentPosition", commentPosition)

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            builder.setContentIntent(pendingIntent)

            //4 Issue the notification
            val notificationManager =
                NotificationManagerCompat.from(context!!)
            notificationManager.notify(Random.nextInt(), builder.build())
        }

        fun getCurrentUserOnlyOnce(){
//            FirebaseApp.initializeApp(context!!)
//            usersRepository.getUserAsRegularObjectNotLiveData(FirebaseAuth.getInstance().currentUser?.uid.toString()).addOnCompleteListener {
//                if (it.isSuccessful){
//                    singletonUser = it.result?.toObject(User::class.java)
//                }
//            }
        }
    }


}