//package com.example.facebook_clone.helper.posthandler
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import com.example.facebook_clone.helper.Utils
//import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
//import com.example.facebook_clone.helper.listener.FriendClickListener
//import com.example.facebook_clone.helper.listener.PostListener
//import com.example.facebook_clone.model.post.Post
//import com.example.facebook_clone.model.post.react.React
//
//class OthersProfileActivityPostsHandler(private val context: Context):
//    BasePostHandler(context), PostListener,CommentsBottomSheetListener,
//    FriendClickListener {
//
//    override fun onReactButtonClicked(
//        post: Post,
//        interactorId: String,
//        interactorName: String,
//        interactorImageUrl: String,
//        reacted: Boolean,
//        currentReact: React?,
//        postPosition: Int
//    ) {
//        currentEditedPostPosition = postPosition
////        if (!reacted) {
////            val myReact = createReact(interactorId, interactorName, interactorImageUrl, 1)
////            addReactToDb(myReact, post).addOnCompleteListener { task ->
////                if (task.isSuccessful) {
////                    notificationsHandler.also {
////                        it.notificationType = "reactOnPost"
////                        it.reactType = 1
////                        it.postId = post.id.orEmpty()
////                        it.handleNotificationCreationAndFiring()
////                    }
////                } else {
////                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
////                }
////            }
////        } else {
////            deleteReactFromPost(
////                currentReact!!,
////                post
////            ).addOnCompleteListener { task ->
////                if (!task.isSuccessful) {
////                    Utils.toastMessage(context, task.exception?.message.toString())
////                }
////            }
////        }
//    }
//
//    override fun onReactButtonLongClicked(
//        post: Post,
//        interactorId: String,
//        interactorName: String,
//        interactorImageUrl: String,
//        reacted: Boolean,
//        currentReact: React?,
//        postPosition: Int
//    ) {
//
//    }
//
//    override fun onCommentButtonClicked(
//        post: Post,
//        interactorId: String,
//        interactorName: String,
//        interactorImageUrl: String,
//        postPosition: Int
//    ) {
//
//    }
//
//    override fun onShareButtonClicked(
//        post: Post,
//        interactorId: String,
//        interactorName: String,
//        interactorImageUrl: String,
//        postPosition: Int
//    ) {
//
//    }
//
//    override fun onReactLayoutClicked(
//        post: Post,
//        interactorId: String,
//        interactorName: String,
//        interactorImageUrl: String,
//        postPosition: Int
//    ) {
//
//    }
//
//    override fun onMediaPostClicked(mediaUrl: String) {
//
//    }
//
//    override fun onPostMoreDotsClicked(post: Post, shared: Boolean?) {
//
//    }
//
//    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {
//
//    }
//    ////////////////////////////////////////////////////////////////////////////////////////////////
//    override fun onAnotherUserCommented(commentPosition: Int, commentId: String, postId: String) {
//
//    }
//    ////////////////////////////////////////////////////////////////////////////////////////////////
//    override fun onFriendClicked(friendId: String) {
//
//    }
//
//}