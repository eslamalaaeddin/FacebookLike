package com.example.facebook_clone.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.Utils.GROUPS_COLLECTION
import com.example.facebook_clone.helper.Utils.GROUP_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.MY_GROUPS
import com.example.facebook_clone.helper.Utils.SPECIFIC_GROUP_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.USERS_COLLECTION
import com.example.facebook_clone.livedata.GroupLiveData
import com.example.facebook_clone.livedata.UserLiveData
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.post.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class GroupsRepository(private val database: FirebaseFirestore,
                       private val auth: FirebaseAuth,
                       private val storage: FirebaseStorage
) {
    private val currentUserId = auth.currentUser?.uid.toString()

    fun createGroup(group: Group): Task<Void> {
        return database.collection(GROUPS_COLLECTION).document(group.id.toString()).set(group)
    }

    fun getGroup(groupId: String): GroupLiveData{
        val documentReference = database.collection(GROUPS_COLLECTION).document(groupId)
        return GroupLiveData(documentReference)
    }

    fun getAllGroups(userId: String): Task<DocumentSnapshot>{
        return database.collection(USERS_COLLECTION).document(userId).get()
    }

    fun addMemberToGroup(member: Member, groupId: String): Task<Void>{
        return database.collection(GROUPS_COLLECTION).document(groupId).update("members", FieldValue.arrayUnion(member))
    }

    fun addGroupToUserGroups(userId: String, semiGroup: SemiGroup): Task<Void>{
        return database.collection(USERS_COLLECTION).document(userId).update("groups", FieldValue.arrayUnion(semiGroup))
    }

    fun getGroupPostsLiveData(groupId: String): LiveData<List<Post>> {
        var posts: MutableList<Post>? = mutableListOf()
        val postsLiveData = MutableLiveData<List<Post>>()
        database.collection(GROUP_POSTS_COLLECTION).document(groupId)
            .collection(SPECIFIC_GROUP_POSTS_COLLECTION)
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { postsSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    posts = postsSnapshot?.toObjects(Post::class.java)
                    postsLiveData.postValue(posts)
                }
            }
        return postsLiveData
    }

}