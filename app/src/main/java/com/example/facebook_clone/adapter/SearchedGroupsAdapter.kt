package com.example.facebook_clone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.SearchedItemListener
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.user.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.searched_item_layout.view.*


class SearchedGroupsAdapter(private var groups: List<Group>, private val searchedItemListener: SearchedItemListener) :
    RecyclerView.Adapter<SearchedGroupsAdapter.SearchedGroupsHolder>() {
    private lateinit var searchedListener: SearchedItemListener

    inner class SearchedGroupsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {


        init {
            searchedListener = searchedItemListener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        fun bind(group: Group) {
            Picasso.get().load(group.coverImageUrl).into(itemView.searchedItemImageView)
            itemView.searchedItemNameTextView.text = group.name
        }

        override fun onClick(item: View?) {
            val searchGroup = groups[adapterPosition]
            searchedListener.onSearchedGroupClicked( searchGroup)
        }

        override fun onLongClick(item: View?): Boolean {
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedGroupsHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.searched_item_layout, parent, false)
        return SearchedGroupsHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    override fun onBindViewHolder(holder: SearchedGroupsHolder, position: Int) {
        val group = groups[holder.adapterPosition]
        holder.bind(group)
    }
}