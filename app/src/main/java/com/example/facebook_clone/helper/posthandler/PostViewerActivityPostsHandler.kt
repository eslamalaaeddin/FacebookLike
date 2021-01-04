package com.example.facebook_clone.helper.posthandler

import android.content.Context
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel

class PostViewerActivityPostsHandler(
    private val context: Context,
    private val postViewModel: PostViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel,
    private val othersProfileActivityViewModel: OthersProfileActivityViewModel
): BasePostHandler(context, postViewModel, notificationsFragmentViewModel, othersProfileActivityViewModel) {

}