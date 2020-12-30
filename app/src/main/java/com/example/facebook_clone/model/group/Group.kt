package com.example.facebook_clone.model.group

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.*

data class Group(
    val id: String? = UUID.randomUUID().toString(),
    var name: String? = null,
    var coverImageUrl: String? = null,
    //exlude as it does not know how to serialze it @get:Exclude
    var admins: List<Member>? = null,
    var joinRequests: List<JoinRequest>? = null,
    var members: List<Member>? = null,
    var blockedMembers: List<Member>? = null,
    val creationTime: Timestamp = Timestamp(Date())
)