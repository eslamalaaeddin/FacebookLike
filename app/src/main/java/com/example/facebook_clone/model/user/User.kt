package com.example.facebook_clone.model.user

import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.user.followed.Followed
import com.example.facebook_clone.model.user.follower.Follower
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.model.user.search.Search
import com.google.firebase.Timestamp
import java.util.*

data class User(
    var id: String? = null,
    var token: String? = null,
    var name: String? = null,
    var gender: String? = null,
    var birthDay: String? = null,
    var profileImageUrl: String? = null,
    var coverImageUrl: String? = null,
    var postsIds: List<String>? = null,
    var friends: List<Friend>? = null,
    var followers: List<Follower>? = null,
    var followings: List<Followed>? = null,
    var groups: List<SemiGroup>? = null,
    var favPagesIds: List<String>? = null,
    var friendRequests: List<FriendRequest>? = null,
    var biography: String? = null,
    var blockedIds: List<String>? = null,
    var notificationsIds: List<String>? = null,
    var statusesIds: List<String>? = null,
    var searches: List<Search>? = null,
    val creationTime: Timestamp = Timestamp(Date())
)
