package com.example.facebook_clone.ui.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.facebook_clone.R
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.edit_comment_bottom_sheet_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditCommentBottomSheet(private val comment: Comment,
                             private val postId: String,
                             private val postPublisherId: String) : BottomSheetDialogFragment() {

    private val postViewModel by viewModel<PostViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.edit_comment_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.get().load(comment.commenterImageUrl).into(userImageView)

        editCommentEditText.setText(comment.attachmentCommentUrl.toString())

        editCommentEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(newComment: Editable?) {
                updateCommentButton.isEnabled = newComment.toString() != comment.attachmentCommentUrl.toString()
            }
        })

        updateCommentButton.setOnClickListener {
            //New comment
            //i can't update field in array so i deleted the old comment and added a new one
            postViewModel.deleteComment(comment, postId, postPublisherId)
            comment.attachmentCommentUrl = editCommentEditText.text.toString()
            postViewModel.updateComment(comment, postId, postPublisherId)
        }

        cancelEditCommentButton.setOnClickListener {
            dismiss()
        }

        upButtonImageView.setOnClickListener {
            dismiss()
        }
    }
}