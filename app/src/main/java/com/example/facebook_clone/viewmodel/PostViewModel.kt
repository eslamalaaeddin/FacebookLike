package com.example.facebook_clone.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.repository.PostsRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.UploadTask

class PostViewModel(private val repository: PostsRepository) : ViewModel() {

    fun createPost(post: Post): Task<Void> {
        return repository.createPost(post)
    }

    fun addSharedPostToMyPosts(post: Post, myId: String): Task<Void> {
        return repository.addSharedPostToMyPosts(post, myId)
    }

    fun getUserProfilePostsLiveData(userId: String): LiveData<List<Post>> {
        return repository.getUserProfilePostsLiveData(userId)
    }

    fun updatePostWithNewEdits(publisherId: String, postId: String, post: Post): Task<Void> {
        return repository.updatePostWithNewEdits(publisherId, postId, post)
    }

    fun updateSharedPostVisibilityWithNewEdits(
        sharerId: String,
        sharedPostId: String,
        post: Post,
        visibility: Int
    ): Task<Void> {
        return repository.updateSharedPostVisibilityWithNewEdits(
            sharerId,
            sharedPostId,
            post,
            visibility
        )
    }

    fun updateTokenInPost(userId: String, token: String) {
        repository.updateTokenInPost(userId, token)
    }

    fun updateTokenInComment(userId: String, token: String) {
        repository.updateTokenInComment(userId, token)
    }


    fun addCommentToPostComments(
        postId: String,
        postPublisherId: String,
        comment: Comment
    ): Task<Void> {
        return repository.addCommentToPostComments(postId, postPublisherId, comment)
    }

    fun getCommentById(commenterId: String, commentId: String): Task<DocumentSnapshot> {
        return repository.getCommentById(commenterId, commentId)
    }

    fun getCommentLiveDataById(
        commenterId: String,
        commentId: String
    ): LiveData<ReactionsAndSubComments> {
        return repository.getCommentLiveDataById(commenterId, commentId)
    }

    fun getCommentUpdates(commenterId: String, commentId: String): DocumentReference? {
        return repository.getCommentUpdates(commenterId, commentId)
    }

    fun addSubCommentToCommentById(
        commenterId: String,
        commentId: String,
        comment: Comment
    ): Task<Void> {
        return repository.addSubCommentToCommentById(commenterId, commentId, comment)
    }

    fun deleteSubCommentFromCommentById(
        commenterId: String,
        superCommentId: String,
        comment: Comment
    ): Task<Void> {
        return repository.deleteSubCommentFromCommentById(commenterId, superCommentId, comment)
    }

    fun deleteCommentDocumentFromCommentsCollection(
        commenterId: String,
        commentId: String
    ): Task<Void> {
        return repository.deleteCommentDocumentFromCommentsCollection(commenterId, commentId)
    }

    fun addCommentIdToCommentsCollection(commenterId: String, commentId: String): Task<Void> {
        return repository.addCommentIdToCommentsCollection(commenterId, commentId)
    }


    fun addReactToReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return repository.addReactToReactsListInCommentDocument(commenterId, commentId, react)
    }

    fun removeReactFromReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return repository.removeReactFromReactsListInCommentDocument(commenterId, commentId, react)
    }

    fun addSubCommentToReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        comment: Comment?
    ): Task<Void> {
        return repository.addSubCommentToReactsListInCommentDocument(
            commenterId,
            commentId,
            comment
        )
    }

    fun removeSubCommentFromReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        comment: Comment?
    ): Task<Void> {
        return repository.removeSubCommentFromReactsListInCommentDocument(
            commenterId,
            commentId,
            comment
        )
    }

    fun getPostById(publisherId: String, postId: String): Task<DocumentSnapshot> {
        return repository.getPostById(publisherId, postId)
    }

    fun deletePost(publisherId: String, postId: String): Task<Void> {
        return repository.deletePost(publisherId, postId)
    }

    fun deleteCommentFromPost(
        comment: Comment,
        postId: String,
        postPublisherId: String
    ): Task<Void> {
        return repository.deleteCommentFromPost(comment, postId, postPublisherId)
    }

    fun updateComment(comment: Comment, postId: String, postPublisherId: String): Task<Void> {
        return repository.updatePostComment(comment, postId, postPublisherId)
    }

    fun addReactToDB(react: React, postId: String, postPublisherId: String): Task<Void> {
        return repository.addReactToDB(react, postId, postPublisherId)
    }

    fun deleteReactFromPost(react: React, postId: String, postPublisherId: String): Task<Void> {
        return repository.deleteReactFromPost(react, postId, postPublisherId)
    }

    fun addShareToPost(share: Share, postId: String, postPublisherId: String): Task<Void> {
        return repository.addShareToPost(share, postId, postPublisherId)
    }

    fun uploadPostImageToCloudStorage(bitmap: Bitmap): UploadTask {
        return repository.uploadPostImageToCloudStorage(bitmap)
    }

    fun uploadPostVideoToCloudStorage(videoUri: Uri): UploadTask {
        return repository.uploadPostVideoToCloudStorage(videoUri)
    }

    fun uploadImageCommentToCloudStorage(bitmap: Bitmap): UploadTask {
        return repository.uploadImageCommentToCloudStorage(bitmap)
    }

    fun uploadVideoCommentToCloudStorage(videoUri: Uri): UploadTask {
        return repository.uploadVideoCommentToCloudStorage(videoUri)
    }

    fun updateReactedValue(postPublisherId: String, postId: String, reacted: Int?): Task<Void> {
        return repository.updateReactedValue(postPublisherId, postId, reacted)
    }

}