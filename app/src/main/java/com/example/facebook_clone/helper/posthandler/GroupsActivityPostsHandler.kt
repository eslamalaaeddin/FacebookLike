package com.example.facebook_clone.helper.posthandler

import android.content.Context
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.viewmodel.fragment.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.activity.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.activity.ProfileActivityViewModel

private const val TAG = "GroupsActivityPostsHand"
private const val FIRST_COLLECTION_TYPE = Utils.GROUP_POSTS_COLLECTION

//private const val CREATOR_REFERENCE_ID =
private const val SECOND_COLLECTION_TYPE = Utils.SPECIFIC_GROUP_POSTS_COLLECTION

class GroupsActivityPostsHandler(
    private val context: Context,
    private val group: Group,
    private val postViewModel: PostViewModel,
    private val profileActivityViewModel: ProfileActivityViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel,
    private val othersProfileActivityViewModel: OthersProfileActivityViewModel
) : BasePostHandler(
    context,
    postViewModel,
    notificationsFragmentViewModel,
    othersProfileActivityViewModel
){

}