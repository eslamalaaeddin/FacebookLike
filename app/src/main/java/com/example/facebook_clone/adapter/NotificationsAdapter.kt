package com.example.facebook_clone.adapter

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.NotificationListener
import com.example.facebook_clone.model.notification.Notification
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.notification_recycled_item_layout.view.*


class NotificationsAdapter(private var notifications: List<Notification>,
                           private val notificationListener: NotificationListener) :
    RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder>() {
    private lateinit var notListener:NotificationListener
    inner class NotificationsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            notListener = notificationListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)


            itemView.confirmFriendRequestButton.setOnClickListener {
                val notificationId = notifications[adapterPosition].id.toString()
                val notifierId =notifications[adapterPosition].notifierId.toString()
                val notifiedId =notifications[adapterPosition].notifiedId.toString()
                val notifierName =notifications[adapterPosition].notifierName.toString()
                val notifierImageUrl =notifications[adapterPosition].notifierImageUrl.toString()
                notListener.onClickConfirmFriendRequestNotification(notifiedId, notificationId,notifierId,notifierName, notifierImageUrl)
                itemView.cancelFriendRequestButton.visibility = View.GONE
                itemView.confirmFriendRequestButton.visibility = View.GONE
            }

            itemView.cancelFriendRequestButton.setOnClickListener {
                val notificationId = notifications[adapterPosition].id.toString()
                val notifiedId =notifications[adapterPosition].notifiedId.toString()
                notListener.onClickDeleteFriendRequestNotification(notifiedId, notificationId)
                itemView.cancelFriendRequestButton.visibility = View.GONE
                itemView.confirmFriendRequestButton.visibility = View.GONE
            }

            itemView.acceptGroupInvitationButton.setOnClickListener {
                val notificationId = notifications[adapterPosition].id.toString()
                val groupId = notifications[adapterPosition].groupId.toString()
                notListener.onAcceptGroupInvitationButtonClicked(groupId, notificationId)
            }
            itemView.cancelGroupInvitationButton.setOnClickListener {
                val notificationId = notifications[adapterPosition].id.toString()
                val notifiedId =notifications[adapterPosition].notifiedId.toString()
                val groupId = notifications[adapterPosition].groupId.toString()
                notListener.onCancelGroupInvitationButtonClicked(notifiedId, notificationId)
            }
        }


        @SuppressLint("SetTextI18n")
        fun bind(notification: Notification) {
            Picasso.get().load(notification.notifierImageUrl).into(itemView.notifierImageView)
            itemView.notificationDateTextView.text =
                DateFormat.format("EEEE, MMM d, h:mm a", notification.notificationTime.toDate())

            if (notification.notificationType == "friendRequest"){
                itemView.notifierName.text = notification.notifierName
                itemView.notificationDescription.text = "sent you a friend request"
                itemView.notificationVisualDescription.setImageResource(R.drawable.ic_friend_request)
                itemView.confirmFriendRequestButton.visibility = View.VISIBLE
                itemView.cancelFriendRequestButton.visibility = View.VISIBLE
                itemView.acceptGroupInvitationButton.visibility = View.GONE
                itemView.cancelGroupInvitationButton.visibility = View.GONE
            }else{
                if (notification.notificationType == "reactOnPost"){
                    val reactType = notification.reactType
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "reacted to your post"
                    when(reactType){
                        1 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_like_react)
                        2 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_love_react)
                        3 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_care_react)
                        4 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_haha_react)
                        5 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_wow_react)
                        6 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_sad_react)
                        7 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_angry_angry)
                    }
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }

                if (notification.notificationType == "reactOnComment"){
                    val reactType = notification.reactType
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "reacted to your comment"
                    when(reactType){
                        1 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_like_react)
                        2 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_love_react)
                        3 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_care_react)
                        4 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_haha_react)
                        5 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_wow_react)
                        6 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_sad_react)
                        7 -> itemView.notificationVisualDescription.setImageResource(R.drawable.ic_angry_angry)
                    }
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }

                if (notification.notificationType == "commentOnPost"){
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "commented on your post"
                    itemView.notificationVisualDescription.setImageResource(R.drawable.ic_notification_comment)
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }

                if (notification.notificationType == "commentOnComment"){
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "replied to your comment"
                    itemView.notificationVisualDescription.setImageResource(R.drawable.ic_notification_comment)
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }

                if (notification.notificationType == "share"){
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "shared your post"
                    itemView.notificationVisualDescription.setImageResource(R.drawable.ic_share_noification)
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }


                if (notification.notificationType == "groupInvitation"){
                    itemView.acceptGroupInvitationButton.visibility = View.VISIBLE
                    itemView.cancelGroupInvitationButton.visibility = View.VISIBLE
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "invited you to join\n${notification.groupName}"
                    itemView.notificationVisualDescription.setImageResource(R.drawable.ic_user_group)
                }
                if (notification.notificationType == "groupPost"){
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "posted in ${notification.groupName}"
                    itemView.notificationVisualDescription.setImageResource(R.drawable.ic_user_group)
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }

                if (notification.notificationType == "groupJoinRequest"){
                    itemView.notifierName.text = notification.notifierName
                    itemView.notificationDescription.text = "wants to join ${notification.groupName}"
                    itemView.notificationVisualDescription.setImageResource(R.drawable.ic_user_group)
                    itemView.confirmFriendRequestButton.visibility = View.GONE
                    itemView.cancelFriendRequestButton.visibility = View.GONE
                    itemView.acceptGroupInvitationButton.visibility = View.GONE
                    itemView.cancelGroupInvitationButton.visibility = View.GONE
                }
            }
        }

        override fun onClick(item: View?) {
            val currentNotification = notifications[adapterPosition]
            val notificationType = currentNotification.notificationType.toString()
            val notifierId = currentNotification.notifierId.toString()
            val notifiedId = currentNotification.notifiedId.toString()
            val postPublisherId = currentNotification.postPublisherId.toString()
            val notifierName = currentNotification.notifierName.toString()
            val notifierImageUrl = currentNotification.notifierImageUrl.toString()

            val firstCollectionType = currentNotification.firstCollectionType
            val creatorReferenceId = currentNotification.creatorReferenceId
            val secondCollectionType = currentNotification.secondCollectionType

            //val whereTheActionOccurred = currentNotification.whereTheActionOccurred.toString()
           // val placeId = currentNotification.placeId.toString()
           // val postPosition = currentNotification.postPosition
            val postId = currentNotification.postId.toString()
//            val commentPosition = currentNotification.commentPosition
            //val commentId = currentNotification.commentId.toString()
            val id = currentNotification.id.toString()
            val groupId = currentNotification.groupId.toString()

            when (notificationType){
                "friendRequest" ->  notListener.onClickFriendRequestNotification(notifierId)
                "reactOnPost" -> notListener.onClickReactOnPostNotification(
                    postPublisherId = postPublisherId,
                    postId = postId,
                    firstCollectionType = firstCollectionType,
                    creatorReferenceId = creatorReferenceId,
                    secondCollectionType = secondCollectionType
                )
                "commentOnPost" -> notListener.onClickCommentOnPostNotification(
                    postPublisherId = postPublisherId,
                    postId = postId,
                    commentPosition = 0,
                    firstCollectionType = firstCollectionType,
                    creatorReferenceId = creatorReferenceId,
                    secondCollectionType = secondCollectionType
                )
                "commentOnComment" -> notListener.onClickCommentOnPostNotification(
                    postPublisherId = postPublisherId,
                    postId = postId,
                    commentPosition = 0,
                    firstCollectionType = firstCollectionType,
                    creatorReferenceId = creatorReferenceId,
                    secondCollectionType = secondCollectionType
                )
                "share" -> notListener.onClickShareOnPostNotification(
                    postPublisherId = postPublisherId,
                    postId = postId,
                    firstCollectionType = firstCollectionType,
                    creatorReferenceId = creatorReferenceId,
                    secondCollectionType = secondCollectionType
                )
                "reactOnComment" -> notListener.onClickReactsOnCommentNotification(
                    postPublisherId = postPublisherId,
                    postId = postId,
                    commentPosition = 0,
                    firstCollectionType = firstCollectionType,
                    creatorReferenceId = creatorReferenceId,
                    secondCollectionType = secondCollectionType
                )

                "groupInvitation" -> {
                    notListener.onClickOnGroupInvitationNotification(notifierId, groupId)
                }

                "groupPost" -> {
                    notListener.onClickCommentOnPostNotification(
                        postPublisherId = postPublisherId,
                        postId = postId,
                        commentPosition = 0,
                        firstCollectionType = firstCollectionType,
                        creatorReferenceId = creatorReferenceId,
                        secondCollectionType = secondCollectionType
                    )
                }
            }

        }

        override fun onLongClick(item: View?): Boolean {
            val notification = notifications[adapterPosition]
            notListener.onNotificationLongClicked(notification)
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_recycled_item_layout, parent, false)

        return NotificationsHolder(view)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: NotificationsHolder, position: Int) {
        val notification = notifications[holder.adapterPosition]
        holder.bind(notification)
    }
}