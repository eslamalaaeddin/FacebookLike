package com.example.facebook_clone.viewmodel.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.repository.UsersRepository
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.search.Search
import com.example.facebook_clone.repository.GroupsRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot


class SearchActivityViewModel(
    private val usersRepository: UsersRepository,
    private val groupsRepository: GroupsRepository
) : ViewModel() {
//    fun searchForUsers(query: String): LiveData<List<User>>? {
//        return usersRepository.getUsersLiveDataAfterSearchingByName(query)
//    }

    fun searchForUsers(): Task<QuerySnapshot> {
        return usersRepository.searchForUsers()
    }

    fun addSearchToRecentSearches(search: Search, searcherId: String): Task<Void> {
        return usersRepository.addSearchToRecentSearches(search, searcherId)
    }

    fun deleteSearchFromRecentSearches(search: Search, searcherId: String): Task<Void> {
        return usersRepository.deleteSearchFromRecentSearches(search, searcherId)
    }

    fun getMe(userId: String): LiveData<User>? {
        return usersRepository.getUserLiveData(userId)
    }

    fun searchForGroups(): Task<QuerySnapshot> {
        return groupsRepository.searchForGroups()
    }

}