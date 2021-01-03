package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.livedata.GroupLiveData
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.repository.GroupsRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class GroupsViewModel(private val groupsRepository: GroupsRepository): ViewModel() {

    fun getGroupLiveData(groupId: String): GroupLiveData{
        return groupsRepository.getGroup(groupId)
    }

    fun getAllGroups(userId: String): Task<DocumentSnapshot>{
        return groupsRepository.getAllGroups(userId)
    }

    fun addMemberToGroup(member: Member, groupId: String): Task<Void>{
        return groupsRepository.addMemberToGroup(member, groupId)
    }

    fun addGroupToUserGroups(userId: String, semiGroup: SemiGroup): Task<Void>{
        return groupsRepository.addGroupToUserGroups(userId, semiGroup)
    }

    fun getGroupPostsLiveData(groupId: String): LiveData<List<Post>> {
        return groupsRepository.getGroupPostsLiveData(groupId)
    }


}