package com.example.facebook_clone.ui.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.CommentDocument
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.react.ReactDocument
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.comments_bottom_sheet.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "CommentsBottomSheet"

class CommentsBottomSheet(
    private val postPublisherId: String,
    private val postId: String,
    private val commenterId: String,
    private val commenterName: String,
    private val imageUrl: String
) : BottomSheetDialogFragment(), CommentClickListener, ReactClickListener {
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private var commentsAdapter: CommentsAdapter =
        CommentsAdapter(auth, commenterName, imageUrl, emptyList(),emptyList(), this, this)

    private var reactClicked = false
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

        updateCommentsUI()

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

    private fun updateCommentsUI() {
        postViewModel.getCommentsByPostId(postPublisherId, postId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.reference?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Utils.toastMessage(requireContext(), error.message.toString())
                        return@addSnapshotListener
                    }

                    val commentsResult = snapshot?.toObject(CommentDocument::class.java)?.comments
                    val reactsResult =   snapshot?.toObject(ReactDocument::class.java)?.reacts

                    val commentsList = commentsResult.orEmpty()
                    val reactsList = reactsResult.orEmpty()


                    commentsAdapter =
                        CommentsAdapter(
                            auth,
                            commenterName,
                            imageUrl,
                            commentsList,
                            reactsList,
                            this,
                            this
                        )
                    //i don't know why it becomes null ==> almost because comments bottom sheet is not vivsible
                    if (commentsRecyclerView != null) {
                        commentsRecyclerView.adapter = commentsAdapter
                    }
                }
            } else {
                Utils.toastMessage(requireContext(), task.exception?.message.toString())
            }
        }

    }

    override fun onCommentLongClicked(comment: Comment) {
        val longClickedCommentBottomSheet =
            LongClickedCommentBottomSheet(comment, postId, postPublisherId)
        longClickedCommentBottomSheet.show(activity?.supportFragmentManager!!, "signature")
    }

    override fun onReactButtonLongClicked() {

    }

    //adding react
    override fun onReactButtonClicked() {
           // createReact()
    }

    //removing react
    override fun onReactButtonClicked(react: React?) {
           // deleteReact(react!!)
    }

    private fun createReact(){
        val react = React(
            reactorId = auth.currentUser?.uid.toString(),
            reactorName = commenterName,
            reactorImageUrl = imageUrl,
            react = 0
        )

        postViewModel.createReact(react, postId, postPublisherId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toastMessage(requireContext(), "React added")
                } else {
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                }
            }
    }

    private fun deleteReact(react: React){
        postViewModel.deleteReact(react, postId, postPublisherId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utils.toastMessage(requireContext(), "React deleted")
            } else {
                Utils.toastMessage(requireContext(), task.exception?.message.toString())
            }
        }
    }


}
