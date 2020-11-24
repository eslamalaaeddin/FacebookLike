package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.search.Search

interface SearchedItemListener {
    fun onSearchedUserClicked(searchedUser: User)
    fun onRecentSearchedItemClicked(search: Search)
    //fun onSearchedPageClicked(search: Search)
    //fun onSearchedGroupClicked(search: Search)
    fun onDeleteSearchIconClicked(search: Search)
}