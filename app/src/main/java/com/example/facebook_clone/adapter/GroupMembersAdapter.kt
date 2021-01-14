package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.GroupMemberListener
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.user.friend.Friend
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.invited_friend_item_layout.view.*
import kotlinx.android.synthetic.main.member_item_layout.view.*

class GroupMembersAdapter(
    private val members: List<Member>,
    private val groupMmbrListener: GroupMemberListener
    ): RecyclerView.Adapter<GroupMembersAdapter.GroupMembersViewHolder>() {
    private val picasso = Picasso.get()
    private var groupMemberListener: GroupMemberListener = groupMmbrListener

    inner class GroupMembersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }
        fun bindMember(member: Member) {
            itemView.memberNameTextView.text = member.name
            picasso.load(member.imageUrl).into(itemView.memberImageView)
        }

        override fun onClick(p0: View?) {
            val member = members[adapterPosition]
            groupMemberListener.onGroupMemberClicked(member)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupMembersAdapter.GroupMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.member_item_layout, parent, false)

        return GroupMembersViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: GroupMembersAdapter.GroupMembersViewHolder,
        position: Int
    ) {
        val member = members[holder.adapterPosition]
        holder.bindMember(member)
    }

    override fun getItemCount(): Int {
        return members.size
    }
}