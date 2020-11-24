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
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.post_configurations_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostConfigurationsBottomSheet(private val post: Post) : BottomSheetDialogFragment() {
    private val postViewModel by viewModel<PostViewModel>()
    private val postPublisherId = post.publisherId.toString()
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

        editPostLayout.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Edit post",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
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
            postViewModel.updatePostWithNewEdits(postPublisherId, postId, post).addOnCompleteListener {
                dialog.dismiss()
                dismiss()
            }
        }

        friendsLayout.setOnClickListener {
            post.visibility = 1
            postViewModel.updatePostWithNewEdits(postPublisherId, postId, post).addOnCompleteListener {
                dialog.dismiss()
                dismiss()
            }
        }

        privateLayout.setOnClickListener {
            post.visibility = 2
            postViewModel.updatePostWithNewEdits(postPublisherId, postId, post).addOnCompleteListener {
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
                postViewModel.deletePost(postPublisherId, postId)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT)
                                .show()
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
            else{
                post.comments!!.forEach { comment ->
                    deleteComment(comment)
                }

                postViewModel.deletePost(postPublisherId, postId)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT)
                                .show()
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