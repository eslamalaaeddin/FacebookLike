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
import com.example.facebook_clone.model.user.friend.Friend
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.notification_recycled_item_layout.view.*
import kotlinx.android.synthetic.main.profile_post_item.view.*


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
                val notifierId =notifications[adapterPosition].notifier?.id.toString()
                val notifierName =notifications[adapterPosition].notifier?.name.toString()
                val notifierImageUrl =notifications[adapterPosition].notifier?.imageUrl.toString()
                notListener.onClickConfirmFriendRequestNotification(notificationId,notifierId,notifierName, notifierImageUrl)
                itemView.cancelFriendRequestButton.visibility = View.GONE
                itemView.confirmFriendRequestButton.visibility = View.GONE
            }

            itemView.cancelFriendRequestButton.setOnClickListener {
                val notificationId = notifications[adapterPosition].id.toString()
                notListener.onClickDeleteFriendRequestNotification(notificationId)
                itemView.cancelFriendRequestButton.visibility = View.GONE
                itemView.confirmFriendRequestButton.visibility = View.GONE
            }
        }


        @SuppressLint("SetTextI18n")
        fun bind(notification: Notification) {
            Picasso.get().load(notification.notifier?.imageUrl).into(itemView.notifierImageView)
            itemView.notificationDateTextView.text =
                DateFormat.format("EEEE, MMM d, h:mm a", notification.notificationTime.toDate())

            if (notification.notificationType == "friendRequest"){
                itemView.notificationDescription.text = "${notification.notifier?.name} sent you a friend request"
                itemView.notificationVisualDescription.setImageResource(R.drawable.ic_friend_request)
                itemView.confirmFriendRequestButton.visibility = View.VISIBLE
                itemView.cancelFriendRequestButton.visibility = View.VISIBLE
            }else{
                itemView.confirmFriendRequestButton.visibility = View.GONE
                itemView.cancelFriendRequestButton.visibility = View.GONE
            }
        }

        override fun onClick(item: View?) {
            val userId = notifications[adapterPosition].notifier?.id.toString()
            notListener.onClickFriendRequestNotification(userId)
        }

        override fun onLongClick(item: View?): Boolean {
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