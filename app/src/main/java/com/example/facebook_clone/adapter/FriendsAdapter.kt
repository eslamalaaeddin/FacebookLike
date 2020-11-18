package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.friend.Friend
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.friend_item_layout.view.*


class FriendsAdapter(private val friends: List<Friend>, private val friendClickListener: FriendClickListener) :
    RecyclerView.Adapter<FriendsAdapter.FriendsHolder>() {
    private lateinit var frClickListener: FriendClickListener
    inner class FriendsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            frClickListener = friendClickListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(friend: Friend) {
            Picasso.get().load(friend.imageUrl).into(itemView.friendImageView)
            itemView.friendNameTextView.text = friend.name
        }

        override fun onClick(item: View?) {
            val friendId = friends[adapterPosition].id.toString()
            frClickListener.onFriendClicked(friendId)
        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_item_layout, parent, false)

        return FriendsHolder(view)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        val friend = friends[holder.adapterPosition]
        holder.bind(friend)
    }
}