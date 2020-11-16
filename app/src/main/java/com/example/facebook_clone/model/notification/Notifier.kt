package com.example.facebook_clone.model.notification

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude

data class Notifier (
    var id: String? = null,
    @get:Exclude var imageBitmap: Bitmap? = null,
    var name: String? = null,
    var imageUrl: String? = null
)
