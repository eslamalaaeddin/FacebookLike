package com.example.facebook_clone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.livedata.GroupLiveData
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.JoinRequest
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.repository.GroupsRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

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

    fun deleteGroupFromUserGroups(member: Member, semiGroup: SemiGroup): Task<Void>{
        return groupsRepository.deleteGroupFromUserGroups(member, semiGroup)
    }

    fun getGroupPostsLiveData(groupId: String): LiveData<List<Post>> {
        return groupsRepository.getGroupPostsLiveData(groupId)
    }

    fun deleteMemberFromGroup(groupId: String, member: Member): Task<Void>{
        return groupsRepository.deleteMember(groupId,member)
    }

    fun sendJoinRequestToGroupAdmins(group: Group?, joinRequest: JoinRequest): Task<Void>{
        return groupsRepository.sendJoinRequestToGroupAdmins(group, joinRequest)
    }

    fun deleteJoinRequest(group: Group, joinRequest: JoinRequest): Task<Void>{
        return groupsRepository.deleteJoinRequest(group, joinRequest)
    }




}