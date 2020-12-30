package com.example.facebook_clone.helper

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

object Utils {

    const val POST_FROM_PROFILE = "fromProfile"
    const val POST_FROM_GROUP = "fromGroup"
    const val POST_FROM_PAGE = "fromPage"

    const val USERS_COLLECTION = "users"
    const val RECENT_USERS_COLLECTION = "recentUsers"
    const val GROUPS_COLLECTION = "groups"
    const val MY_GROUPS = "my_groups"
    const val PAGES_COLLECTION = "pages"
    const val POSTS_COLLECTION = "posts"
    const val COMMENTS_COLLECTION = "comments"
    const val MY_COMMENTS_COLLECTION = "my_comments"
    const val NOTIFICATIONS_COLLECTION = "notifications"
    const val MY_NOTIFICATIONS_COLLECTION = "my_notifications"
    const val PROFILE_POSTS_COLLECTION = "my_posts"
    const val GROUP_POSTS_COLLECTION = "group posts collection"
    const val SPECIFIC_GROUP_POSTS_COLLECTION = "specific_group_posts_collection"
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

    //Void Task version
    fun doAfterFinishing(context: Context, task: Task<Void>, successMessage: String){
        if (task.isSuccessful){
            toastMessage(context, successMessage)
        }
        else{
            toastMessage(context, task.exception?.message.toString())
        }
    }

}