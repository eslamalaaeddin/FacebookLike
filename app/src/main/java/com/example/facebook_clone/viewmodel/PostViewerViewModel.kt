package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.repository.PostsRepository
import com.example.facebook_clone.repository.UsersRepository

class PostViewerViewModel(
    private val usersRepository: UsersRepository,
    private val postsRepository: PostsRepository
) : ViewModel() {
    private var postLiveData: LiveData<Post>? = null

    fun getPostLiveData(postPublisherId: String, postId: String): LiveData<Post>? {
        var liveData = postLiveData
        if (liveData == null) {
            liveData = postsRepository.getPostLiveDataById(postPublisherId, postId)
            postLiveData = liveData
        }
        return liveData
    }
}