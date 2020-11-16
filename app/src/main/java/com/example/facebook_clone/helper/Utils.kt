package com.example.facebook_clone.helper

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast

object Utils {

    const val USERS_COLLECTION = "users"
    const val GROUPS_COLLECTION = "groups"
    const val PAGES_COLLECTION = "pages"
    const val POSTS_COLLECTION = "posts"
    const val NOTIFICATIONS_COLLECTION = "notifications"
    const val MY_NOTIFICATIONS_COLLECTION = "my_notifications"
    const val PROFILE_POSTS_COLLECTION = "my_posts"
//    const val OTHERS_POSTS_COLLECTION = "others_posts"
    const val STATUSES_COLLECTION = "statuses"


    fun toastMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showProgressDialog(context: Context, message: String): ProgressDialog{
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(message)
        progressDialog.show()
        return progressDialog
    }

}