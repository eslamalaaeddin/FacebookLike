package com.example.facebook_clone.helper.posthandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task

private const val TAG = "GroupsActivityPostsHand"
private const val FIRST_COLLECTION_TYPE = Utils.GROUP_POSTS_COLLECTION
//private const val CREATOR_REFERENCE_ID =
private const val SECOND_COLLECTION_TYPE = Utils.SPECIFIC_GROUP_POSTS_COLLECTION
class GroupsActivityPostsHandler(
    private val context: Context,
    private val groupId: String,
    private val postViewModel: PostViewModel
    ): BasePostHandler(context, postViewModel), PostListener {

    override fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {
//        currentEditedPostPosition = postPosition
//        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, groupId, SECOND_COLLECTION_TYPE)
//        if (!reacted) {
//            val myReact = createReact(interactorId, interactorName, interactorImageUrl)
//            addReactOnPostToDb(myReact, modifiedPost).addOnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        else {
//            deleteReactFromPost(
//                currentReact!!,
//                modifiedPost
//            ).addOnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Utils.toastMessage(context, task.exception?.message.toString())
//                }
//            }
//        }
    }

    override fun onReactButtonLongClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {

    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
//        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, groupId, SECOND_COLLECTION_TYPE)
//        openCommentsBottomSheet(modifiedPost, interactorId, interactorName, interactorImageUrl, postPosition)
    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        Toast.makeText(context, "Post Shared", Toast.LENGTH_SHORT).show()
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
//        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, groupId, SECOND_COLLECTION_TYPE)
//        openCommentsBottomSheet(modifiedPost, interactorId, interactorName, interactorImageUrl, postPosition)
    }

    override fun onMediaPostClicked(mediaUrl: String) { handleMediaClicks(mediaUrl) }

    override fun onPostMoreDotsClicked(post: Post, shared: Boolean?) {
        Toast.makeText(context, "Dots Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {
        Toast.makeText(context, "Shared Post", Toast.LENGTH_SHORT).show()
    }

//    private fun addReactOnPostToDb(
//        react: React,
//        post: Post
//    ): Task<Void> {
//        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, groupId, SECOND_COLLECTION_TYPE)
//        return postViewModel.addReactToDB(react, modifiedPost)
//    }
//
//    private fun deleteReactFromPost(
//        react: React,
//        post: Post
//    ): Task<Void> {
//        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, groupId, SECOND_COLLECTION_TYPE)
//        return postViewModel.deleteReactFromPost(react, modifiedPost)
//    }

//    private fun openCommentsBottomSheet(post: Post,
//                                        interactorId: String,
//                                        interactorName: String,
//                                        interactorImageUrl: String,
//                                        postPosition: Int){
//
//        currentEditedPostPosition = postPosition
//        val commentsBottomSheet = CommentsBottomSheet(
//            post,
//            interactorId,
//            interactorName,
//            interactorImageUrl,
//            null,//used to handle notification so, no need for it in my profile.
//            ""//me
//        )
//        commentsBottomSheet.show((context as AppCompatActivity).supportFragmentManager, commentsBottomSheet.tag)
//    }


}