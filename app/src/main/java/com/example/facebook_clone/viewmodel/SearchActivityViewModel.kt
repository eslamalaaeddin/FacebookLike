package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.repository.UsersRepository
import com.example.facebook_clone.model.user.User


class SearchActivityViewModel(private val usersRepository: UsersRepository) : ViewModel() {
     fun searchForUsers(query: String): LiveData<List<User>>?{
        return usersRepository.searchForUsers(query)
    }

}