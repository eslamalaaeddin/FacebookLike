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
import com.example.facebook_clone.ui.activity.*
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
                    recentUsersActivityViewModelModule,
                    postViewerActivityViewModelModule,
                    groupsFragmentViewModel,
                    groupsRepositoryModule,
                    groupsViewModelModule
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
        fun fireClientSideNotification(
            notificationType: String,
            notifier: Notifier,
            postId: String?,
            postPublisherId: String?,
            commentPosition: Int?,
            notifiedId: String?,
            firstCollectionType: String?,
            creatorReferenceId: String?,
            secondCollectionType: String?,
            groupName: String?,
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

                "commentOnComment" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} replied to your Comment"
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

                "groupInvitation" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} invited you to join $groupName"
                    )
                    destination = GroupActivity::class.java
                }

                "groupPost" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} posted in $groupName"
                    )
                    destination = PostViewerActivity::class.java
                }

                "groupJoinRequest" -> {
                    remoteView.setTextViewText(
                        R.id.notificationContentTextView,
                        "${notifier.name} wants to join $groupName"
                    )
                    destination = NewsFeedActivity::class.java
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

            actionIntent.putExtra("firstCollectionType", firstCollectionType)
            actionIntent.putExtra("creatorReferenceId", creatorReferenceId)
            actionIntent.putExtra("secondCollectionType", secondCollectionType)
//            actionIntent.putExtra("firstCollectionType", firstCollectionType)
            actionIntent.putExtra("friendRequester", notifier.id.toString())
            actionIntent.putExtra("postPublisherId", postPublisherId)
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

    }


}