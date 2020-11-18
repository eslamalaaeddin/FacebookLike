package com.example.facebook_clone.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.facebook_clone.helper.Utils.POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.PROFILE_POSTS_COLLECTION
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.*

private const val TAG = "PostsRepository"

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
        val query = database.collection(POSTS_COLLECTION).document(userId)
            .collection(PROFILE_POSTS_COLLECTION)
            .orderBy("creationTime", Query.Direction.DESCENDING)

        return FirestoreRecyclerOptions
            .Builder<Post>()
            .setQuery(query, Post::class.java)
            .build()
    }

    fun getPostsWithoutOptions(userId: String): LiveData<List<Post>> {
        var posts: MutableList<Post>?
        val liveData = MutableLiveData<List<Post>>()
        database.collection(POSTS_COLLECTION).document(userId)
            .collection(PROFILE_POSTS_COLLECTION)
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { postsSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    posts = postsSnapshot?.toObjects(Post::class.java)
                    liveData.postValue(posts)
                }
            }
        return liveData
    }

    fun createComment(postId: String, postPublisherId: String, comment: Comment): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId)
            .collection(PROFILE_POSTS_COLLECTION).document(postId)
            .update("comments", FieldValue.arrayUnion(comment))
    }

    //better to be named getPostSnapshotByItsId
    fun getPostById(publisherId: String, postId: String): Task<DocumentSnapshot> {
        return database.collection(POSTS_COLLECTION).document(publisherId)
            .collection(PROFILE_POSTS_COLLECTION)
            .document(postId).get()

    }

    fun deleteComment(comment: Comment, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("comments", FieldValue.arrayRemove(comment))
    }

    fun updateComment(comment: Comment, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("comments", FieldValue.arrayUnion(comment))
    }

    fun createReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacts", FieldValue.arrayUnion(react))
    }

    fun deleteReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacts", FieldValue.arrayRemove(react))
    }

    fun getReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacts", FieldValue.arrayRemove(react))
    }

    fun createShare(share: Share, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("shares", FieldValue.arrayUnion(share))
    }

    fun uploadPostImageToCloudStorage(bitmap: Bitmap): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post images").child("${UUID.randomUUID()}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }

    fun uploadPostVideoToCloudStorage(videoUri: Uri): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post videos").child("${UUID.randomUUID()}.mp4")

        return firebaseStorageRef.putFile(videoUri)

    }

    fun uploadImageCommentToCloudStorage(bitmap: Bitmap): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post images").child("comments").child("${UUID.randomUUID()}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }

    fun uploadVideoCommentToCloudStorage(videoUri: Uri): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post videos").child("comments").child("${UUID.randomUUID()}.mp4")

        return firebaseStorageRef.putFile(videoUri)

    }


}