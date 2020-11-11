package com.example.facebook_clone.ui.bottomsheet

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.CommentClickListener
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.Comment
import com.example.facebook_clone.model.post.CommentDocument
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.comments_bottom_sheet.*
import okhttp3.internal.Util
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "CommentsBottomSheet"

class CommentsBottomSheet(
    private val postPublisherId: String,
    private val postId: String,
    private val commenterId: String,
    private val commenterName: String,
    private val imageUrl: String
) : BottomSheetDialogFragment(), CommentClickListener{
    private val postViewModel by viewModel<PostViewModel>()
    private var commentsAdapter: CommentsAdapter = CommentsAdapter(emptyList(),this)

    //private lateinit var comments: List<Comment>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.comments_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateComments()

        commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(comment: Editable?) {
                //Implement the case where user comment with an image only
                if (comment?.isNotEmpty()!!) {
                    sendCommentImageView.visibility = View.VISIBLE
                } else {
                    sendCommentImageView.visibility = View.GONE
                }
            }
        })

        sendCommentImageView.setOnClickListener {
            //
            //ADD COMMENT TO DATABASE VV//
            //SHOW IT IN RECYCLER VIEW VV//
            //NOTIFY THE USER
            val commentContent = commentEditText.text.toString()

            val comment = Comment(
                commenterId = commenterId,
                commenterName = commenterName,
                commenterImageUrl = imageUrl,
                comment = commentContent,
                commentType = "text"
            )

            postViewModel.createComment(postId, postPublisherId, comment)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        commentEditText.text.clear()
                    } else {
                        Utils.toastMessage(requireContext(), task.exception?.message.toString())
                    }
                }

        }

    }

    private fun updateComments() {
        postViewModel.getCommentsByPostId(postPublisherId, postId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.reference?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Utils.toastMessage(requireContext(), error.message.toString())
                        return@addSnapshotListener
                    }

                    val commentsList = snapshot?.toObject(CommentDocument::class.java)?.comments
                    commentsAdapter = CommentsAdapter(commentsList!!,this)
                    commentsRecyclerView.adapter = commentsAdapter
                }
            } else {
                Utils.toastMessage(requireContext(), task.exception?.message.toString())
            }
        }

    }

    override fun onCommentLongClicked(comment: Comment) {
        val longClickedCommentBottomSheet = LongClickedCommentBottomSheet(comment, postId, postPublisherId)
        longClickedCommentBottomSheet.show(activity?.supportFragmentManager!!, "signature")
    }


}
