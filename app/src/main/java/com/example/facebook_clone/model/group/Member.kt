package com.example.facebook_clone.model.group

import com.google.firebase.Timestamp
import java.util.*

data class Member(
    var id: String? = null,
    var name: String? = null,
    var imageUrl: String? = null,
    var blocked: Boolean? = false,
    var joinTime: Timestamp = Timestamp(Date())
)