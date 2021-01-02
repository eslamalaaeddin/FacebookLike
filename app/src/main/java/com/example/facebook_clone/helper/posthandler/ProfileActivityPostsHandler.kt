package com.example.facebook_clone.helper.posthandler

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.Utils.POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.PROFILE_POSTS_COLLECTION
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.ui.activity.OthersProfileActivity
import com.example.facebook_clone.ui.activity.PostViewerActivity
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.PostConfigurationsBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*

private const val FIRST_COLLECTION_TYPE = POSTS_COLLECTION
private const val SECOND_COLLECTION_TYPE = PROFILE_POSTS_COLLECTION
class ProfileActivityPostsHandler(
    private val fromWhere: String,
    private val context: Context,
    private val postViewModel: PostViewModel,
    private val profileActivityViewModel: ProfileActivityViewModel
    ): BasePostHandler(context, postViewModel), PostListener, FriendClickListener {

    override fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        if (!reacted) {
            val myReact = createReact(interactorId, interactorName, interactorImageUrl)
            addReactOnPostToDb(interactorId, myReact, modifiedPost)
        }
        else {
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
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        currentEditedPostPosition = postPosition
        showReactsChooserDialog(
            interactorId,
            interactorName,
            interactorImageUrl,
            modifiedPost,
            currentReact
        )
    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        openCommentsBottomSheet(modifiedPost, interactorId, interactorName, interactorImageUrl, postPosition)
    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        currentEditedPostPosition = postPosition
        val share = Share(
            sharerId = interactorId,
            sharerName = interactorName,
            sharerImageUrl = interactorImageUrl,
        )
        addShareToPost(share, modifiedPost).addOnCompleteListener { task ->
            Utils.doAfterFinishing(context, task, "You shared this post")
        }
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        openCommentsBottomSheet(modifiedPost, interactorId, interactorName, interactorImageUrl, postPosition)
    }

    override fun onMediaPostClicked(mediaUrl: String) { handleMediaClicks(mediaUrl) }

    override fun onPostMoreDotsClicked(post: Post, shared: Boolean?) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        openPostConfigurationsBottomSheet(modifiedPost, shared)
    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {
        val intent = Intent(context, PostViewerActivity::class.java)
        intent.putExtra("postPublisherId", originalPostPublisherId)
        intent.putExtra("postId", postId)
        context.startActivity(intent)
    }

    override fun onFriendClicked(friendId: String) {
        val intent = Intent(context, OthersProfileActivity::class.java)
        intent.putExtra("userId", friendId)
        context.startActivity(intent)
    }

    fun uploadCoverImageToCloudStorage(bitmap: Bitmap){
        progressDialog = Utils.showProgressDialog(context, "Please wait...")
        profileActivityViewModel.uploadCoverImageToCloudStorage(bitmap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                        progressDialog?.dismiss()
                        uploadCoverImageToUserCollection(photoUrl.toString())
                    }
                }
            }
    }

    fun uploadProfileImageToCloudStorage(bitmap: Bitmap){
        progressDialog = Utils.showProgressDialog(context, "Please wait...")
        profileActivityViewModel.uploadProfileImageToCloudStorage(bitmap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                        progressDialog?.dismiss()
                        uploadProfileImageToUserCollection(photoUrl.toString())
                    }
                }
            }
    }

    private fun uploadCoverImageToUserCollection(photoUrl: String) {
        profileActivityViewModel
            .uploadCoverImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                Utils.doAfterFinishing(
                    context,
                    task,
                    "Image uploaded successfully"
                )
            }
    }

    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profileActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                Utils.doAfterFinishing(context, task, "Image uploaded successfully")

            }
    }
}