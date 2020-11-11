package com.example.facebook_clone.repository

import com.example.facebook_clone.helper.Utils.POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.PROFILE_POSTS_COLLECTION
import com.example.facebook_clone.model.post.Comment
import com.example.facebook_clone.model.post.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage

class PostsRepository(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    fun createPost(post: Post): Task<Void> {
        //Strings only
        return database.collection(POSTS_COLLECTION).document(post.publisherId.toString())
            .collection(PROFILE_POSTS_COLLECTION).document(post.id.toString()).set(post)
    }

    fun getPostsByUserId(userId: String): FirestoreRecyclerOptions<Post> {
        //1 creating the query
        val query = database.collection(POSTS_COLLECTION).document(userId)
            .collection(PROFILE_POSTS_COLLECTION)
//            .whereEqualTo("publisherId", userId)
            .orderBy("creationTime", Query.Direction.DESCENDING)

        //2 creating the options
        return FirestoreRecyclerOptions
            .Builder<Post>()
            .setQuery(query, Post::class.java)
            .build()
    }

    fun createComment(postId: String,postPublisherId: String,comment: Comment): Task<Void>{
        return database.collection(POSTS_COLLECTION).document(postPublisherId)
            .collection(PROFILE_POSTS_COLLECTION).document(postId)
            .update("comments", FieldValue.arrayUnion(comment))
    }

    fun getCommentsByPostId(publisherId: String, postId: String): Task<DocumentSnapshot> {
        return database.collection(POSTS_COLLECTION).document(publisherId)
            .collection(PROFILE_POSTS_COLLECTION)
            .document(postId).get()

    }

    fun deleteComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION).document(postId).update("comments",FieldValue.arrayRemove(comment))
    }

    fun updateComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION).document(postId).update("comments", FieldValue.arrayUnion(comment))
    }

}