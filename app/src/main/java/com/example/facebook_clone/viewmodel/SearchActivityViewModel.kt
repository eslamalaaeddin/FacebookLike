package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.repository.UsersRepository
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.search.Search
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue


class SearchActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {
     fun searchForUsers(query: String): LiveData<List<User>>?{
        return usersRepository.getUsersLiveDataAfterSearchingByName(query)
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

}