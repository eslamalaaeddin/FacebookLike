package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.long_clicked_comment_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LongClickedCommentBottomSheet(private val comment: Comment,
                                    private val postId: String,
                                    private val postPublisherId: String): BottomSheetDialogFragment() {

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
            val editCommentBottomSheet = EditCommentBottomSheet(comment, postId, postPublisherId)
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
            deleteComment(comment, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun deleteComment(comment: Comment, postId: String, postPublisherId: String){
        postViewModel.deleteComment(comment, postId, postPublisherId).addOnCompleteListener { task ->
            if (!task.isSuccessful){
                Utils.toastMessage(requireContext(), task.exception?.message.toString())
            }
        }
    }
}