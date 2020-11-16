package com.example.facebook_clone.helper.listener

interface SearchedItemListener {
    fun onSearchedUserClicked(userId: String)
    fun onSearchedPageClicked(pageId: String)
    fun onSearchedGroupClicked(groupId: String)
}