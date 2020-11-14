package com.example.facebook_clone.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.repository.PostsRepository
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.UploadTask

class PostViewModel(private val repository: PostsRepository): ViewModel() {

    fun createPost(post: Post): Task<Void>{
        return repository.createPost(post)
    }

    fun getPostsByUserId(userId: String) : FirestoreRecyclerOptions<Post> {
        return repository.getPostsByUserId(userId)
    }

    fun getPostsWithoutOptions(userId: String) : LiveData<List<Post>> {
        return repository.getPostsWithoutOptions(userId)
    }

    fun createComment(postId: String,postPublisherId: String, comment: Comment): Task<Void>{
        return repository.createComment(postId,postPublisherId, comment)
    }

    fun getPostById(publisherId: String, postId: String): Task<DocumentSnapshot>{
        return repository.getPostById(publisherId, postId)
    }

    fun deleteComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return repository.deleteComment(comment, postId, postPublisherId)
    }

    fun updateComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return repository.updateComment(comment, postId, postPublisherId)
    }

    fun createReact(react: React, postId: String, postPublisherId: String): Task<Void>{
        return repository.createReact(react, postId, postPublisherId)
    }

    fun deleteReact(react: React, postId: String, postPublisherId: String): Task<Void>{
        return repository.deleteReact(react, postId, postPublisherId)
    }

    fun createShare(share: Share, postId: String, postPublisherId: String): Task<Void>{
        return repository.createShare(share, postId, postPublisherId)
    }

    fun uploadPostImageToCloudStorage(bitmap: Bitmap): UploadTask{
        return repository.uploadPostImageToCloudStorage(bitmap)
    }

    fun uploadPostVideoToCloudStorage(videoUri: Uri): UploadTask{
        return repository.uploadPostVideoToCloudStorage(videoUri)
    }

}