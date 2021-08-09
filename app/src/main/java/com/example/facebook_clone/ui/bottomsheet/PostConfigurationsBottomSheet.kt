package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.activity.ProfileActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.post_configurations_bottom_sheet.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostConfigurationsBottomSheet(private val post: Post, private val shared: Boolean?) : BottomSheetDialogFragment() {
    private val postViewModel by viewModel<PostViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val auth: FirebaseAuth by inject()

    private lateinit var postPublisherId: String
    private val postId = post.id.toString()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.post_configurations_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (shared != null) {
            if (shared) {
                postPublisherId = post.shares?.get(0)?.sharerId.toString()
            }
        }
            else{
                postPublisherId = post.publisherId.toString()
            }


        // TODO: 8/4/2021 Bad Code of course, but i won't touch it |:
        editPostLayout.setOnClickListener {
            val userLiveDate = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
            userLiveDate?.observe(this, { user ->
                user?.let {
                    val postCreatorDialog = PostCreatorDialog(Utils.POST_FROM_PROFILE, currentUser = it, postToBeEdited = post)
                    postCreatorDialog.show(activity?.supportFragmentManager!!, "signature")
                    postCreatorDialog.setUserNameAndProfileImageUrl(
                        it.name.toString(),
                        it.profileImageUrl.toString()
                    )
                }
            })
        }

        editPostPrivacyLayout.setOnClickListener {
            showPostVisibilityOptions()
        }

        deletePostLayout.setOnClickListener {
            showDeletePostConfirmationDialog()
        }
    }

    private fun showPostVisibilityOptions() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.change_post_visibility_dialog)

        val publicLayout = dialog.findViewById(R.id.publicVisibilityLayout) as LinearLayout
        val friendsLayout = dialog.findViewById(R.id.friendsVisibilityLayout) as LinearLayout
        val privateLayout = dialog.findViewById(R.id.privateVisibilityLayout) as LinearLayout

        publicLayout.setOnClickListener {
            post.visibility = 0
            postViewModel.updatePostWithNewEdits(post).addOnCompleteListener {
                if (!post.shares.isNullOrEmpty()){
                    post.shares?.let {shares ->
                        shares.forEach { share ->
                            val sharerId = share.sharerId.toString()
                            val sharedPostId = share.id.toString()
                            post.visibility = 0
                            val updatedPost = post
                            postViewModel.updateSharedPostVisibilityWithNewEdits(sharerId, sharedPostId, updatedPost, 0)

                        }
                    }
                }
                dialog.dismiss()
                dismiss()
            }
        }

        friendsLayout.setOnClickListener {
            post.visibility = 1
            postViewModel.updatePostWithNewEdits(post).addOnCompleteListener {
                if (!post.shares.isNullOrEmpty()){
                    post.shares?.let {shares ->
                        shares.forEach { share ->
                            val sharerId = share.sharerId.toString()
                            val sharedPostId = share.id.toString()
                            post.visibility = 1
                            val updatedPost = post
                            postViewModel.updateSharedPostVisibilityWithNewEdits(sharerId, sharedPostId, updatedPost, 1)

                        }
                    }
                }
                dialog.dismiss()
                dismiss()
            }
        }

        privateLayout.setOnClickListener {
            post.visibility = 2
            postViewModel.updatePostWithNewEdits(post).addOnCompleteListener {
                if (!post.shares.isNullOrEmpty()){
                    post.shares?.let {shares ->
                        shares.forEach { share ->
                            val sharerId = share.sharerId.toString()
                            val sharedPostId = share.id.toString()
                            post.visibility = -1
                            val updatedPost = post
                            postViewModel.updateSharedPostVisibilityWithNewEdits(sharerId, sharedPostId, updatedPost, -1)

                        }
                    }
                }
                dialog.dismiss()
                dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeletePostConfirmationDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_post_dialog)

        val deleteButton = dialog.findViewById(R.id.deletePostTextView) as TextView
        val cancelButton = dialog.findViewById(R.id.cancelPostDeletionTextView) as TextView
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        deleteButton.setOnClickListener {
            //[1]get post to loop through its comments and delete them one by one
            if (post.comments == null || post.comments!!.isEmpty()) {
                postViewModel.deletePost(post)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
//                            dialog.dismiss()
//                            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT)
//                                .show()
//                            dismiss()
                        }
                        else {
                            Toast.makeText(
                                requireContext(),
                                it.exception?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }
            else{
                post.comments!!.forEach { comment ->
                    deleteComment(comment)
                }
                postViewModel.deletePost(post).addOnCompleteListener {
                        if (it.isSuccessful) {
                            dialog.dismiss()
                            dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                it.exception?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }

            if (post.shares.isNullOrEmpty()){
                postViewModel.deletePost(post).addOnCompleteListener {
                    if (it.isSuccessful) {
                     //   Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(
                            requireContext(),
                            it.exception?.message,
                            Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
                dismiss()
            }
            else{
                post.shares?.let {shares ->
                    shares.forEach { share ->
                        val sharerId = share.sharerId.toString()
                        postViewModel.deletePost(post).addOnCompleteListener {
                            if (it.isSuccessful) {
//                                dialog.dismiss()
//                                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT)
//                                    .show()

                            }
                            else {
                                Toast.makeText(
                                    requireContext(),
                                    it.exception?.message,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }
                dialog.dismiss()
                dismiss()
            }
        }
        dialog.show()

    }

    private fun deleteComment(comment: Comment) {
        //[1]Get super comment
        postViewModel.getCommentById(comment.commenterId.toString(), comment.id.toString())
            .addOnCompleteListener {
                val returnedComment = it.result?.toObject(ReactionsAndSubComments::class.java)

                if (returnedComment?.subComments != null) {
                    //[2]loop on super comment sub comments if exists
                    returnedComment.subComments!!.forEach { subComment ->
                        //[3]delete super comment's document
                        postViewModel.deleteCommentDocumentFromCommentsCollection(
                            subComment.commenterId.toString(),
                            subComment.id.toString()
                        )

                            .addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    //[5]delete super comment document
                                    postViewModel.deleteCommentDocumentFromCommentsCollection(
                                        comment.commenterId.toString(),
                                        comment.id.toString()
                                    )
                                } else {
                                    Utils.toastMessage(
                                        requireContext(),
                                        task1.exception?.message.toString()
                                    )
                                }
                            }
                    }
                } else {
                    postViewModel.deleteCommentDocumentFromCommentsCollection(
                        comment.commenterId.toString(),
                        comment.id.toString()
                    )

                }

            }
    }
}