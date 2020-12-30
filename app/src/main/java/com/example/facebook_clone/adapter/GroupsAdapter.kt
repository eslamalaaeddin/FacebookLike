package com.example.facebook_clone.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.helper.listener.GroupsItemListener
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.ui.activity.GroupActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.friend_item_layout.view.*
import kotlinx.android.synthetic.main.group_item_layout.view.*

class GroupsAdapter(
    private val groups: List<SemiGroup>,
    private val groupsItemListener: GroupsItemListener
) : RecyclerView.Adapter<GroupsAdapter.GroupsHolder>() {
    private lateinit var gItemListener: GroupsItemListener
    private val picasso = Picasso.get()

    inner class GroupsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            gItemListener = groupsItemListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(group: SemiGroup) {
            picasso.load(group.coverUrl).into(itemView.groupImageView)
            itemView.groupNameTextView.text = group.name
        }

        override fun onClick(item: View?) {
            val group = groups[adapterPosition]
            gItemListener.onGroupClicked(group.id.toString())
        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_item_layout, parent, false)

        return GroupsHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    override fun onBindViewHolder(holder: GroupsHolder, position: Int) {
        val group = groups[holder.adapterPosition]
        holder.bind(group)
    }
}