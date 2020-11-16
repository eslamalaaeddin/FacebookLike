package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.search.Search
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.friend_item_layout.view.*

class RecentSearchesAdapter(private val searchedItems: List<Search>) :
    RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchHolder>() {

    inner class RecentSearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(searchItem: Search) {

        }

        override fun onClick(item: View?) {

        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_item_layout, parent, false)

        return RecentSearchHolder(view)
    }

    override fun getItemCount(): Int {
        return searchedItems.size
    }

    override fun onBindViewHolder(holder: RecentSearchHolder, position: Int) {
        val searchedItem = searchedItems[holder.adapterPosition]
        holder.bind(searchedItem)
    }
}