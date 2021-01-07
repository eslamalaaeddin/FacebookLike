package com.example.facebook_clone.helper.posthandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.SharedPost
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task

private const val TAG = "GroupsActivityPostsHand"
private const val FIRST_COLLECTION_TYPE = Utils.GROUP_POSTS_COLLECTION

//private const val CREATOR_REFERENCE_ID =
private const val SECOND_COLLECTION_TYPE = Utils.SPECIFIC_GROUP_POSTS_COLLECTION

class GroupsActivityPostsHandler(
    private val context: Context,
    private val group: Group,
    private val postViewModel: PostViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel?,
    private val othersProfileActivityViewModel: OthersProfileActivityViewModel?
) : BasePostHandler(
    context,
    postViewModel,
    notificationsFragmentViewModel,
    othersProfileActivityViewModel
), PostListener, CommentsBottomSheetListener {

    //    private val notificationsHandler = NotificationsHandler(
//        notificationsFragmentViewModel = notificationsFragmentViewModel,
//        othersProfileActivityViewModel = othersProfileActivityViewModel)
    override fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String?
    ) {
        currentEditedPostPosition = postPosition
        val modifiedPost =
            handlePostLocation(
                post,
                FIRST_COLLECTION_TYPE,
                group.id.orEmpty(),
                SECOND_COLLECTION_TYPE
            )
        if (!reacted) {
            val myReact = createReact(interactorId, interactorName, interactorImageUrl)
            addReactOnPostToDb(interactorId, myReact, modifiedPost)
        } else {
            deleteReactFromPost(currentReact!!, modifiedPost).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Utils.toastMessage(context, task.exception?.message.toString())
                }
            }
        }
    }

    override fun onReactButtonLongClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String?
    ) {
        val modifiedPost = handlePostLocation(
            post,
            FIRST_COLLECTION_TYPE,
            post.groupId.orEmpty(),
            SECOND_COLLECTION_TYPE
        )
        currentEditedPostPosition = postPosition
        showReactsChooserDialog(
            interactorId,
            interactorName,
            interactorImageUrl,
            modifiedPost,
            currentReact,
            null
        )
    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(
            post,
            FIRST_COLLECTION_TYPE,
            group.id.orEmpty(),
            SECOND_COLLECTION_TYPE
        )
        openCommentsBottomSheet(
            modifiedPost,
            interactorId,
            interactorName,
            interactorImageUrl,
            postPosition,
            this
        )
    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String?
    ) {
        Toast.makeText(context, "لا يا أخي؟!", Toast.LENGTH_SHORT).show()
    }

    private fun addSharedPostToMyPosts(post: Post, myId: String) {
        postViewModel.addSharedPostToMyPosts(post, myId)
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(
            post,
            FIRST_COLLECTION_TYPE,
            group.id.orEmpty(),
            SECOND_COLLECTION_TYPE
        )
        openCommentsBottomSheet(
            modifiedPost,
            interactorId,
            interactorName,
            interactorImageUrl,
            postPosition,
            this
        )
    }

    override fun onMediaPostClicked(mediaUrl: String) {
        handleMediaClicks(mediaUrl)
    }

    override fun onPostMoreDotsClicked(interactorId: String, post: Post, shared: Boolean?) {
        val admin = group.admins.orEmpty().first()
        if (admin.id == interactorId) {
            Toast.makeText(context, "Show admin tools", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Show member tools", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {
        Toast.makeText(context, "Shared Post", Toast.LENGTH_SHORT).show()
    }

    override fun onAnotherUserCommented(
        notifierId: String,
        notifierName: String,
        notifierImageUrl: String,
        notifiedId: String,
        notifiedToken: String,
        notificationType: String,
        postPublisherId: String,
        postId: String ,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentId: String
    ) {
//        notificationsHandler.also {
//            it.notificationType = "commentOnPost"
//            it.postId = postId
//            it.commentPosition = commentPosition
//            it.handleNotificationCreationAndFiring()
//        }
        Toast.makeText(context, "Notify Her", Toast.LENGTH_SHORT).show()
    }

}