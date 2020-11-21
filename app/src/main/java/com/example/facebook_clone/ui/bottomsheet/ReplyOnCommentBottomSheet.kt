package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.helper.provider.ReplyOnCommentDataProvider
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.*
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.commentEditText
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.myReactPlaceHolder
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.sendCommentImageView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "ReplyOnCommentBottomShe"

class ReplyOnCommentBottomSheet(
    private val clicksConsumer: ReplyOnCommentDataProvider,
    private val postPublisherId: String,
    private val comment: Comment,
    private val commentPosition: Int,
    private val commenterId: String,
    private val commenterName: String,
    private val commenterImageUrl: String,
    private val reacted: Boolean,
    private val currentReact: React?
) : BottomSheetDialogFragment(), CommentClickListener, ReactClickListener {
    private val postViewModel by viewModel<PostViewModel>()
    private lateinit var commentsAdapter: CommentsAdapter
    private val auth: FirebaseAuth by inject()
    private val picasso = Picasso.get()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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
        return layoutInflater.inflate(
            R.layout.reply_on_comment_bottom_sheet_layout,
            container,
            false
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        upButtonImageView.setOnClickListener { dismiss() }

        commentEditText.requestFocus()

        updateCommentsUI()

        sendCommentImageView.setOnClickListener {
            val commentContent = commentEditText.text.toString()

            if (commentContent.isEmpty()){
                Toast.makeText(requireContext(), "Add comment first", Toast.LENGTH_SHORT).show()
            }
            else{
                val comment =
                    createComment(
                        commentContent = commentContent,
                        commentType = "text",
                        attachmentCommentUrl = null
                    )

                commentEditText.text.clear()
                postViewModel.addSubCommentToCommentById(this.comment.commenterId.toString(), this.comment.id.toString(),comment )
                    .addOnCompleteListener {
                        //NOTIFICATION

                        if (!it.isSuccessful){
                            Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            updateCommentsUI()
        }

        mediaCommentImageView.setOnClickListener {
            val mediaUrl = comment.attachmentCommentUrl.toString()
            if (mediaUrl.contains("jpeg")) {
                val imageViewerDialog = ImageViewerDialog()
                imageViewerDialog.show(activity?.supportFragmentManager!!, "signature")
                imageViewerDialog.setMediaUrl(mediaUrl)
            } else {
                val videoIntent = Intent(requireContext(), VideoPlayerActivity::class.java)
                videoIntent.putExtra("videoUrl", mediaUrl)
                startActivity(videoIntent)
            }
        }

        reactOnCommentTextView.setOnClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false

            postViewModel.getCommentById(postPublisherId, comment.id.toString())
                .addOnCompleteListener {
                    val commentDoc =
                        it.result?.toObject(ReactionsAndSubComments::class.java)
                    commentDoc?.reactions?.let { reacts ->
                        for (react in reacts) {
                            if (react.reactorId == auth.currentUser?.uid.toString()) {
                                currentReact = react
                                reacted = true
                                break
                            }
                        }

                        clicksConsumer.reactOnCommentFromRepliesDataProvider(
                            comment,
                            commentPosition,
                            reacted,
                            currentReact,
                            "click"
                        )
                    }
                    updateCommentsUI()

                }

        }

        reactOnCommentTextView.setOnLongClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false

            postViewModel.getCommentById(postPublisherId, comment.id.toString())
                .addOnCompleteListener {
                    val commentDoc =
                        it.result?.toObject(ReactionsAndSubComments::class.java)
                    commentDoc?.reactions?.let { reacts ->
                        for (react in reacts) {
                            if (react.reactorId == auth.currentUser?.uid.toString()) {
                                currentReact = react
                                reacted = true
                                break
                            }
                        }

                        clicksConsumer.reactOnCommentFromRepliesDataProvider(
                            comment,
                            commentPosition,
                            reacted,
                            currentReact,
                            "longClick"
                        )
                    }
                    updateCommentsUI()

                }
            true
        }

        replyOnCommentTextView.setOnClickListener {  commentEditText.requestFocus() }
    }

        private fun updateCommentsUI() {
            val commentLiveData = postViewModel.getCommentLiveDataById(postPublisherId, comment.id.toString())
            commentLiveData.observe(viewLifecycleOwner, {reactionsAndComments ->
                //REACTIONS
                reactionsAndComments?.reactions?.let { reacts ->
                    if (reacts.isNotEmpty()) {
                        reactsCountTextView.text = reacts.size.toString()
                        reactsCountTextView.visibility = View.VISIBLE
                        for (react in reacts) {
                            if (react.reactorId == auth.currentUser?.uid.toString()) {
                                myReactPlaceHolder.visibility = View.VISIBLE
//                                itemView.likeReactPlaceHolder.visibility = View.GONE
//                                itemView.loveReactPlaceHolder.visibility = View.GONE
//                                itemView.careReactPlaceHolder.visibility = View.GONE
//                                itemView.hahaReactPlaceHolder.visibility = View.GONE
//                                itemView.wowReactPlaceHolder.visibility = View.GONE
//                                itemView.sadReactPlaceHolder.visibility = View.GONE
//                                itemView.angryReactPlaceHolder.visibility = View.GONE
                                when (react.react) {
                                    1 -> {
                                        reactOnCommentTextView.text = "Like"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.dark_blue
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_like_react)
                                    }
                                    2 -> {
                                        reactOnCommentTextView.text = "Love"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.red
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_love_react)
                                    }
                                    3 -> {
                                        reactOnCommentTextView.text = "Care"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_care_react)
                                    }
                                    4 -> {
                                        reactOnCommentTextView.text = "Haha"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_haha_react)
                                    }
                                    5 -> {
                                        reactOnCommentTextView.text = "Wow"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_wow_react)
                                    }
                                    6 -> {
                                        reactOnCommentTextView.text = "Sad"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.yellow
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_sad_react)
                                    }
                                    7 -> {
                                        reactOnCommentTextView.text = "Angry"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_angry_angry)
                                    }
                                }
//                                itemView.reactOnCommentTextView.text = "Like"
//                                itemView.reactOnCommentTextView.setTextColor(
//                                    itemView.context.resources.getColor(
//                                        R.color.dark_blue
//                                    )
//                                )
                                break
                            }
                            else {
                                //Visible to me
                                myReactPlaceHolder.visibility = View.INVISIBLE
                                //itemView.likeReactPlaceHolder.visibility = View.GONE
//                                itemView.loveReactPlaceHolder.visibility = View.GONE
//                                itemView.careReactPlaceHolder.visibility = View.GONE
//                                itemView.hahaReactPlaceHolder.visibility = View.GONE
//                                itemView.wowReactPlaceHolder.visibility = View.GONE
//                                itemView.sadReactPlaceHolder.visibility = View.GONE
//                                itemView.angryReactPlaceHolder.visibility = View.GONE
                                reactOnCommentTextView.text = "Like"
                                when (react.react) {
                                    1 -> {
                                        likeReactPlaceHolder.visibility = View.VISIBLE
                                        loveReactPlaceHolder.visibility = View.GONE
                                        careReactPlaceHolder.visibility = View.GONE
                                        hahaReactPlaceHolder.visibility = View.GONE
                                        wowReactPlaceHolder.visibility = View.GONE
                                        sadReactPlaceHolder.visibility = View.GONE
                                        angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    2 -> {
                                        likeReactPlaceHolder.visibility = View.GONE
                                        loveReactPlaceHolder.visibility = View.VISIBLE
                                        careReactPlaceHolder.visibility = View.GONE
                                        hahaReactPlaceHolder.visibility = View.GONE
                                        wowReactPlaceHolder.visibility = View.GONE
                                        sadReactPlaceHolder.visibility = View.GONE
                                        angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    3 -> {
                                        likeReactPlaceHolder.visibility = View.GONE
                                        loveReactPlaceHolder.visibility = View.GONE
                                        careReactPlaceHolder.visibility = View.VISIBLE
                                        hahaReactPlaceHolder.visibility = View.GONE
                                        wowReactPlaceHolder.visibility = View.GONE
                                        sadReactPlaceHolder.visibility = View.GONE
                                        angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    4 -> {
                                        likeReactPlaceHolder.visibility = View.GONE
                                        loveReactPlaceHolder.visibility = View.GONE
                                        careReactPlaceHolder.visibility = View.GONE
                                        hahaReactPlaceHolder.visibility = View.VISIBLE
                                        wowReactPlaceHolder.visibility = View.GONE
                                        sadReactPlaceHolder.visibility = View.GONE
                                        angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    5 -> {
                                        likeReactPlaceHolder.visibility = View.GONE
                                        loveReactPlaceHolder.visibility = View.GONE
                                        careReactPlaceHolder.visibility = View.GONE
                                        hahaReactPlaceHolder.visibility = View.GONE
                                        wowReactPlaceHolder.visibility = View.VISIBLE
                                        sadReactPlaceHolder.visibility = View.GONE
                                        angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    6 -> {
                                        likeReactPlaceHolder.visibility = View.GONE
                                        loveReactPlaceHolder.visibility = View.GONE
                                        careReactPlaceHolder.visibility = View.GONE
                                        hahaReactPlaceHolder.visibility = View.GONE
                                        wowReactPlaceHolder.visibility = View.GONE
                                        sadReactPlaceHolder.visibility = View.VISIBLE
                                        angryReactPlaceHolder.visibility = View.GONE
                                    }
                                    7 -> {
                                        likeReactPlaceHolder.visibility = View.GONE
                                        loveReactPlaceHolder.visibility = View.GONE
                                        careReactPlaceHolder.visibility = View.GONE
                                        hahaReactPlaceHolder.visibility = View.GONE
                                        wowReactPlaceHolder.visibility = View.GONE
                                        sadReactPlaceHolder.visibility = View.GONE
                                        angryReactPlaceHolder.visibility = View.VISIBLE
                                    }
                                }
                            }

                        }
                    }else{
                        myReactPlaceHolder.visibility = View.INVISIBLE
                        reactsCountTextView.visibility = View.INVISIBLE

                        reactOnCommentTextView.text = "Like"
                        reactOnCommentTextView.setTextColor(
                            activity?.resources?.getColor(
                                R.color.gray
                            )!!)
                    }

                }

                //SUBCOMMENTS
                if (reactionsAndComments.subComments != null){
                    commentsAdapter =CommentsAdapter(
                        auth.currentUser?.uid.toString(),
                        reactionsAndComments.subComments!!,
                        null,
                        this,
                        this,
                        postViewModel,
                        postPublisherId
                    )

                    subCommentsRecyclerView.adapter = commentsAdapter
                }
            })


            picasso.load(comment.commenterImageUrl).into(commenterImageView)
            commenterNameTextView.text = comment.commenterName
            commentCreationTimeTextView.text =
                DateFormat.format("EEE, MMM d, h:mm a", comment.commentTime.toDate())

            val interval: Long = 1 * 1000
            val options: RequestOptions = RequestOptions().frame(interval)

            if (comment.commentType == "text") {
                commentTextView.text = comment.textComment
                mediaCommentCardView.visibility = View.GONE
            } else if (comment.commentType == "textWithImage") {
                commentTextView.text = comment.textComment
                picasso.load(comment.attachmentCommentUrl).into(mediaCommentImageView)
                mediaCommentCardView.visibility = View.VISIBLE
            } else if (comment.commentType == "textWithVideo") {
                commentTextView.text = comment.textComment
                Glide.with(requireContext())
                    .asBitmap().load(comment.attachmentCommentUrl).apply(options)
                    .into(mediaCommentImageView)
                mediaCommentCardView.visibility = View.VISIBLE
            } else if (comment.commentType == "image") {
                picasso.load(comment.attachmentCommentUrl).into(mediaCommentImageView)
                mediaCommentCardView.visibility = View.VISIBLE
            } else if (comment.commentType == "video") {
                Glide.with(requireContext())
                    .asBitmap().load(comment.attachmentCommentUrl).apply(options)
                    .into(mediaCommentImageView)
                mediaCommentCardView.visibility = View.VISIBLE
            }

        }

    override fun onCommentLongClicked(comment: Comment) {

    }

    override fun onReactOnCommentClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        Toast.makeText(requireContext(), "React Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onReactOnCommentLongClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        Toast.makeText(requireContext(), "React long Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onReplyToCommentClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        commentEditText.requestFocus()
    }

    override fun onCommentReactionsLayoutClicked(commentId: String) {

    }

    override fun onMediaCommentClicked(mediaUrl: String) {
        //Image
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show(activity?.supportFragmentManager!!, "signature")
            imageViewerDialog.setMediaUrl(mediaUrl)
        }
        //video(I chosed an activity to show media controllers)
        else {
            val videoIntent = Intent(requireContext(), VideoPlayerActivity::class.java)
            videoIntent.putExtra("videoUrl", mediaUrl)
            startActivity(videoIntent)
        }
    }

    override fun onReactButtonLongClicked() {

    }

    override fun onReactButtonClicked() {

    }

    override fun onReactButtonClicked(react: React?) {
    }

    private fun createComment(
        attachmentCommentUrl: String?,
        commentContent: String?,
        commentType: String
    ): Comment {
        return Comment(
            commenterId = commenterId,
            commenterName = commenterName,
            commenterImageUrl = commenterImageUrl,
            textComment = commentContent,
            commentType = commentType,
            attachmentCommentUrl = attachmentCommentUrl
        )
    }
}
