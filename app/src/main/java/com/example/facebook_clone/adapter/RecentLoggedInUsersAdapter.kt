package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.RecentUsersClickListener
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recent_logged_in_user_layout.view.*


class RecentLoggedInUsersAdapter(
    private var users: List<RecentLoggedInUser>,
    private val recentUsersClickListener: RecentUsersClickListener
) :
    RecyclerView.Adapter<RecentLoggedInUsersAdapter.RecentLoggedInUsersHolder>() {
    private lateinit var rcntUserClickListener: RecentUsersClickListener

    inner class RecentLoggedInUsersHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            rcntUserClickListener = recentUsersClickListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(user: RecentLoggedInUser) {
            Picasso.get().load(user.imageUrl).into(itemView.recentLoggedInUserImageView)
            itemView.recentLoggedInUserNameTextView.text = user.name
        }

        override fun onClick(item: View?) {
            val curreyUser = users[adapterPosition]
            rcntUserClickListener.onRecentUserClicked(curreyUser.email.toString(), curreyUser.password.toString())
        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentLoggedInUsersHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_logged_in_user_layout, parent, false)

        return RecentLoggedInUsersHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: RecentLoggedInUsersHolder, position: Int) {
        val user = users[holder.adapterPosition]
        holder.bind(user)
    }
}