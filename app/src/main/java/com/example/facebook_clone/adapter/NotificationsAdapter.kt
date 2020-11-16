package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.notification.Notification


class NotificationsAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder>() {

    inner class NotificationsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(notification: Notification) {

        }

        override fun onClick(item: View?) {

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