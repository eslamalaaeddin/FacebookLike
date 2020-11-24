package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.SearchedItemListener
import com.example.facebook_clone.model.user.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.searched_item_layout.view.*


class SearchedUsersAdapter(private var users: List<User>, private val searchedItemListener: SearchedItemListener) :
    RecyclerView.Adapter<SearchedUsersAdapter.SearchedUsersHolder>() {
    private lateinit var searchedListener: SearchedItemListener

    inner class SearchedUsersHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            searchedListener = searchedItemListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(user: User) {
            Picasso.get().load(user.profileImageUrl).into(itemView.searchedItemImageView)
            itemView.searchedItemNameTextView.text = user.name
        }

        override fun onClick(item: View?) {
            val searchUser = users[adapterPosition]
           searchedListener.onSearchedUserClicked( searchUser)
        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedUsersHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.searched_item_layout, parent, false)
        return SearchedUsersHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: SearchedUsersHolder, position: Int) {
        val user = users[holder.adapterPosition]
        holder.bind(user)
    }
}