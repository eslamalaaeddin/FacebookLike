package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.SearchedItemListener
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.search.Search
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.friend_item_layout.view.*
import kotlinx.android.synthetic.main.recent_searched_item_layout.view.*
import kotlinx.android.synthetic.main.searched_item_layout.view.*
import kotlinx.android.synthetic.main.searched_item_layout.view.searchedItemImageView
import kotlinx.android.synthetic.main.searched_item_layout.view.searchedItemNameTextView

class RecentSearchesAdapter(private val searchedItems: List<Search>, private val searchedItemListener: SearchedItemListener) :
    RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchHolder>() {
    private lateinit var searchedListener: SearchedItemListener
    inner class RecentSearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            searchedListener = searchedItemListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)

            itemView.deleteSearchImageView.setOnClickListener {
                val searchItem = searchedItems[adapterPosition]
                searchedListener.onDeleteSearchIconClicked(searchItem)
            }
        }


        fun bind(searchItem: Search) {
            Picasso.get().load(searchItem.searchedImageUrl).into(itemView.searchedItemImageView)
            itemView.searchedItemNameTextView.text = searchItem.searchedName
        }

        override fun onClick(item: View?) {
            val searchItem = searchedItems[adapterPosition]
            searchedListener.onRecentSearchedItemClicked(searchItem)
        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_searched_item_layout, parent, false)

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