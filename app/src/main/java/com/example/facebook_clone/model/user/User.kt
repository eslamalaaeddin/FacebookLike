package com.example.facebook_clone.model.user

import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.model.user.search.Search
import com.google.firebase.Timestamp
import java.util.*

data class User(
    var id: String? = null,
    var name: String? = null,
    var gender: String? = null,
    var birthDay: String? = null,
    var profileImageUrl: String? = null,
    var coverImageUrl: String? = null,
    var postsIds: List<String>? = null,//list of
    var friendsIds: List<String>? = null,//list of
    var followersIds: List<String>? = null,//list of
    var followingIds: List<String>? = null,//list of
    var groupsIds: List<String>? = null,//list of
    var favPagesIds: List<String>? = null,//list of
    var friendRequests: List<FriendRequest>? = null,
    var biography: String? = null,
    var blockedIds: List<String>? = null,//list of
    var notificationsIds: List<String>? = null,//list of
    var statusesIds: List<String>? = null,//list of
    var searches: List<Search>? = null,
    val creationTime: Timestamp = Timestamp(Date())

)
