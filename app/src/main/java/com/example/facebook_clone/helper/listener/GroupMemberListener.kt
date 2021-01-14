package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.group.Member

interface GroupMemberListener {
    fun onGroupMemberClicked(member: Member)
}