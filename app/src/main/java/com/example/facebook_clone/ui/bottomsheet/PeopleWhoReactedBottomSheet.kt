package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.ReactsAdapter
import com.example.facebook_clone.adapter.SearchedUsersAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.ReactDocument
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.people_who_reacted_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PeopleWhoReactedBottomS"

class PeopleWhoReactedBottomSheet(
    private val commentId: String?,
    private val postId: String,
    private val postPublisherId: String,
    private val reactedOn: String
) :
    BottomSheetDialogFragment() {
    private val postViewModel by viewModel<PostViewModel>()
    private var reactsAdapter = ReactsAdapter(emptyList())
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //To make it full screen
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.people_who_reacted_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        upButtonImageView.setOnClickListener { dismiss() }

        if (reactedOn == "post"){
            postViewModel.getPostById(postPublisherId, postId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reactsOnPost =
                        task.result?.toObject(ReactDocument::class.java)?.reacts.orEmpty()
                    reactsAdapter = ReactsAdapter(reactsOnPost)
                    peopleWhoReactedRecyclerView.adapter = reactsAdapter
                }else{
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                }
            }
        }
        else if (reactedOn == "comment"){
            postViewModel.getCommentById(postPublisherId, commentId = commentId.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val comment = task.result?.toObject(ReactionsAndSubComments::class.java)
                    reactsAdapter = ReactsAdapter(comment?.reactions.orEmpty())
                    peopleWhoReactedRecyclerView.adapter = reactsAdapter
                }else{
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                }
            }
        }

//        postViewModel.getPostById(postPublisherId, postId).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                //POST REACTS
//                if (reactedOn == "post") {
//                    val reactsOnPost =
//                        task.result?.toObject(ReactDocument::class.java)?.reacts.orEmpty()
//                    reactsAdapter = ReactsAdapter(reactsOnPost)
//                    peopleWhoReactedRecyclerView.adapter = reactsAdapter
//                }
//                //COMMENT REACTS
//                else {
//                    val post = task.result?.toObject(Post::class.java)
//                    post?.comments?.forEach { comment ->
//                        if (comment.id == "commentId") {
//                            reactsAdapter = ReactsAdapter(comment.reacts.orEmpty())
//                            peopleWhoReactedRecyclerView.adapter = reactsAdapter
//                        }
//                    }
//                }
//            } else {
//                Utils.toastMessage(requireContext(), task.exception?.message.toString())
//            }
//        }

    }
}