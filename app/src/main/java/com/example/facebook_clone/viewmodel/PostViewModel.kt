package com.example.facebook_clone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.repository.PostsRepository
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task

class PostViewModel(private val repository: PostsRepository): ViewModel() {

    fun createPost(post: Post): Task<Void>{
        return repository.createPost(post)
    }

    fun getPostsByUserId(userId: String) : FirestoreRecyclerOptions<Post> {
        return repository.getPostsByUserId(userId)
    }

}