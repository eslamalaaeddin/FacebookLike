package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.InvitedFriendsListener
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.user.friend.Friend
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.invited_friend_item_layout.view.*

class InvitedFriendsAdapter(
    private val group: Group,
    private val friends: List<Friend>,
    private val invitedFriendListener: InvitedFriendsListener
    ):
    RecyclerView.Adapter<InvitedFriendsAdapter.InvitedFriendsViewHolder>() {
    private val picasso = Picasso.get()
    private lateinit var invitedFriendLstnr: InvitedFriendsListener

     inner class InvitedFriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
         init {
             invitedFriendLstnr = invitedFriendListener
             itemView.inviteFriendButton.setOnClickListener {
                 val currentFriend = friends[adapterPosition]
                 //I forgot to desing the Friend model so that it contains friend Token, so will query it in the invite
                 //members bottom sheet
                 val currentFriendId = currentFriend.id.toString()
                 invitedFriendLstnr.onInviteButtonClicked(currentFriendId, group.name.orEmpty())
             }
         }

        fun bindFriends(friend: Friend){
                for (member in group.members.orEmpty()){
                    if (friend.id == member.id){
                        itemView.visibility = View.GONE
                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                    }
                    else{
                        itemView.visibility = View.VISIBLE
                        itemView.layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                        itemView.invitedFriendNameTextView.text = friend.name
                        picasso.load(friend.imageUrl).into(itemView.smallUserImageView)
                    }
                }


        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InvitedFriendsAdapter.InvitedFriendsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.invited_friend_item_layout, parent, false)

        return InvitedFriendsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: InvitedFriendsAdapter.InvitedFriendsViewHolder,
        position: Int
    ) {
        val friend = friends[holder.adapterPosition]
        holder.bindFriends(friend)
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}