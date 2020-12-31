package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.long_clicked_comment_bottom_sheet.*
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "LongClickedCommentBotto"

class LongClickedCommentBottomSheet(
    private val superComment: Comment?,
    private val comment: Comment,
    private val post: Post,
    private val type: String
) : BottomSheetDialogFragment() {
    private val postViewModel by viewModel<PostViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.long_clicked_comment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        replyCommentLayout.setOnClickListener {

        }

        copyCommentLayout.setOnClickListener {

        }

        deleteCommentLayout.setOnClickListener {
            showDeleteCommentDialog()
        }

        editCommentLayout.setOnClickListener {
            val editCommentBottomSheet = EditCommentBottomSheet(comment, post)
            editCommentBottomSheet.show(activity?.supportFragmentManager!!, "signature")
        }

    }

    private fun showDeleteCommentDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.delete_comment_dialog_layout)

        val cancelButton = dialog.findViewById(R.id.cancelDeleteCommentButton) as TextView
        val deleteButton = dialog.findViewById(R.id.deleteCommentButton) as TextView
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        deleteButton.setOnClickListener {
            if (type == "comment") {
                deleteComment(comment, post)
            } else if (type == "subComment") {
                deleteSubCommentAndItsDocument(comment)

            }
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun deleteComment(comment: Comment, post: Post) {
        //[1]Get super comment
        postViewModel.getCommentById(comment.commenterId.toString(), comment.id.toString())
            .addOnCompleteListener {
                val returnedComment = it.result?.toObject(ReactionsAndSubComments::class.java)
                if (returnedComment?.subComments != null) {
                    //[2]loop on super comment sub comments if exists
                    returnedComment.subComments!!.forEach { subComment ->
                        //[3]delete super comment's document
                        postViewModel
                            .deleteCommentDocumentFromCommentsCollection(
                                subComment.commenterId.toString(),
                                subComment.id.toString()
                            )
                            .addOnCompleteListener {
                                //[4]delete super comment from post
                                postViewModel.deleteCommentFromPost(
                                    comment,
                                    post
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
                                        dismiss()
                                    }
                            }
                    }
                }
                else {
                    postViewModel.deleteCommentFromPost(comment, post)
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
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
                            dismiss()
                        }
                }
            }

    }

    private fun deleteSubCommentAndItsDocument(comment: Comment) {
            postViewModel.deleteSubCommentFromCommentById(
                superComment?.commenterId.toString(),
                superComment?.id.toString(),
                comment
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    postViewModel
                        .deleteCommentDocumentFromCommentsCollection(
                            comment.commenterId.toString(),
                            comment.id.toString()
                        )
                } else {
                    Toast.makeText(requireContext(), task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                }
                dismiss()
            }

    }


}


