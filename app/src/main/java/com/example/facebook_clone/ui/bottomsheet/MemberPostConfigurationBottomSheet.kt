package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.member_post_configuration_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MemberPostConfigurationBottomSheet(private val post: Post?, private val memberOrAdmin: String) : BottomSheetDialogFragment() {
    private val groupsViewModel by viewModel<GroupsViewModel>()
    private val postViewModel by viewModel<PostViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.member_post_configuration_bottom_sheet, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (memberOrAdmin == "admin"){
            if (post != null) {
                if (post.commentsAvailable) {
                    turnOffPostingCommentingLayout.visibility = View.VISIBLE
                    turnOnPostingCommentingLayout.visibility = View.GONE
                } else {
                    turnOffPostingCommentingLayout.visibility = View.GONE
                    turnOnPostingCommentingLayout.visibility = View.VISIBLE
                }
            }
            deletePostLayout.visibility = View.VISIBLE
            editPostLayout.visibility = View.GONE
            reportPostLayout.visibility = View.GONE
        }

        else{
            //I am the poster
            if (post != null){
                reportPostLayout.visibility = View.GONE
                if (post.commentsAvailable){
                    turnOffPostingCommentingLayout.visibility = View.GONE
                    turnOnPostingCommentingLayout.visibility = View.VISIBLE
                }
                else{
                    turnOffPostingCommentingLayout.visibility = View.VISIBLE
                    turnOnPostingCommentingLayout.visibility = View.GONE
                }
//                Toast.makeText(requireContext(), "From post creator", Toast.LENGTH_SHORT).show()
            }

            else{
//                Toast.makeText(requireContext(), "Not from post creator", Toast.LENGTH_SHORT).show()
                reportPostLayout.visibility = View.VISIBLE
                editPostLayout.visibility = View.GONE
                turnOffPostingCommentingLayout.visibility = View.GONE
                turnOnPostingCommentingLayout.visibility = View.GONE
                deletePostLayout.visibility = View.GONE
            }
        }


        reportPostLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Report", Toast.LENGTH_SHORT).show()
        }

        editPostLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Edit", Toast.LENGTH_SHORT).show()
        }

        turnOffPostingCommentingLayout.setOnClickListener {
            post?.let {
                groupsViewModel.turnOffPostCommenting(it).addOnCompleteListener { task ->
                    if(!task.isSuccessful){
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                    dismiss()
                }
            }
        }

        turnOnPostingCommentingLayout.setOnClickListener {
            post?.let {
                groupsViewModel.turnOnPostCommenting(it).addOnCompleteListener { task ->
                    if(!task.isSuccessful){
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                    dismiss()
                }
            }
        }

        reportPostLayout.setOnClickListener {
            Toast.makeText(requireContext(), "مش عايز دي في دي ؟!", Toast.LENGTH_SHORT).show()
        }

        deletePostLayout.setOnClickListener {
            post?.let {showDeletePostConfirmationDialog(it)}
        }

    }

    private fun showDeletePostConfirmationDialog(post: Post) {
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
                            Toast.LENGTH_SHORT).show()
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