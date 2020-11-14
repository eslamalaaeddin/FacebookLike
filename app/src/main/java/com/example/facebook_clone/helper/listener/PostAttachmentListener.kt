package com.example.facebook_clone.helper.listener

import android.content.Intent

interface PostAttachmentListener {
    fun onAttachmentAdded(data: Intent?, dataType: String, fromCamera:Boolean)
}