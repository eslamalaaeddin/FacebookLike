package com.example.facebook_clone.viewmodel.fragment

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.repository.GroupsRepository
import com.google.android.gms.tasks.Task

class GroupsFragmentViewModel(private val groupsRepository: GroupsRepository): ViewModel() {

    fun createGroup(group: Group): Task<Void>{
        return groupsRepository.createGroup(group)
    }
}