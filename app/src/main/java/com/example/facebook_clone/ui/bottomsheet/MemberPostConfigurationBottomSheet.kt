package com.example.facebook_clone.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.model.post.Post
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.member_post_configuration_bottom_sheet.*


class MemberPostConfigurationBottomSheet(private val post: Post?, private val memberOrAdmin: String) : BottomSheetDialogFragment() {

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
                    turnOffPostingCommentingLayout.visibility = View.GONE
                    turnOnPostingCommentingLayout.visibility = View.VISIBLE
                } else {
                    turnOffPostingCommentingLayout.visibility = View.VISIBLE
                    turnOnPostingCommentingLayout.visibility = View.GONE
                }
            }
            deletePostLayout.visibility = View.VISIBLE
            editPostLayout.visibility = View.GONE
            reportPostLayout.visibility = View.GONE
        }

        else{
            //I am the poster
            if (post != null){
                reportPostLayout.visibility = View.VISIBLE
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



        editPostLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Edit", Toast.LENGTH_SHORT).show()
        }

        turnOffPostingCommentingLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Turn off", Toast.LENGTH_SHORT).show()
        }

        turnOnPostingCommentingLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Turn on", Toast.LENGTH_SHORT).show()
        }

        reportPostLayout.setOnClickListener {
            Toast.makeText(requireContext(), "مش عايز دي في دي ؟!", Toast.LENGTH_SHORT).show()
        }

        deletePostLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Delete", Toast.LENGTH_SHORT).show()
        }

    }

}