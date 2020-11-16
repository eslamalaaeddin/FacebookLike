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
import com.example.facebook_clone.R
import com.example.facebook_clone.di.*
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.ui.activity.MainActivity
import com.google.api.Billing
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
private const val CHANNEL_ID = "123"
private const val CHANNEL_NAME = "channel name"
private const val NOTIFICATION_ID = 123
private const val NOTIFICATION_TITLE = "My Notification"
private const val NOTIFICATION_CONTENT = "My Notification Content"

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
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
                    othersProfileActivityViewModelModule
                )
            )
        }

    }

    private fun createNotificationChannel(){
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
        var context: Context? = null
        fun fireNotification(notification: Notification, destination: Class<*>) {
            val sound: Uri =
                Uri.parse("android.resource://" + context?.packageName + "/" + R.raw.facebook_notification_sound)

            val remoteView = RemoteViews(context?.packageName, R.layout.custom_notification_layout)

            remoteView.setTextViewText(R.id.notificationContentTextView,
                "${notification.notifier?.name} sent you a friend request")
            remoteView.setImageViewBitmap(R.id.notificationImageView, notification.notifier?.imageBitmap)

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
                .setVibrate(longArrayOf(0, 500, 800, 500))
                .setAutoCancel(true)
            //3 Create the action
            val actionIntent = Intent(context, destination)
            val pendingIntent =
                PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setContentIntent(pendingIntent)

            //4 Issue the notification
            val notificationManager =
                NotificationManagerCompat.from(context!!)
            notificationManager.notify((Math.random()*100).toInt(), builder.build())
        }
    }
}