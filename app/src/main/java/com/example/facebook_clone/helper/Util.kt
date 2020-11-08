package com.example.facebook_clone.helper

import android.content.Context
import android.os.Message
import android.widget.Toast

object Util {
    fun toastMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}