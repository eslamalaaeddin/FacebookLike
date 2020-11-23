package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.PostAttachmentListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.helper.provider.ReplyOnCommentDataProvider
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.activity.VideoPlayerActivity
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_item_layout.view.*
import kotlinx.android.synthetic.main.comments_bottom_sheet.*
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.*
import kotlinx.android.synthetic.main.reply_on_comment_bottom_sheet_layout.addAttachmentToComment
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
    private val commenterToken: String,//the person whom you are replying to
    private val postId: String,
    private val reacted: Boolean,
    private val currentReact: React?
) : BottomSheetDialogFragment(),
    CommentClickListener,
    ReactClickListener,
    PostAttachmentListener
{
    private val postViewModel by viewModel<PostViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var notificationsHandler: NotificationsHandler
    private lateinit var comBottomSheetListener: CommentsBottomSheetListener
    private val auth: FirebaseAuth by inject()
    private var commentData: Intent? = null
    private var commentDataType: String? = null
    private var bitmapFromCamera: Boolean = false
    private var commentAttachmentUrl: String? = null
    private val picasso = Picasso.get()
    private var progressDialog: ProgressDialog? = null
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

        notificationsHandler = NotificationsHandler(
            othersProfileActivityViewModel = othersProfileActivityViewModel,
            notificationsFragmentViewModel = notificationsFragmentViewModel
        )

        notificationsHandler.notifierId = commenterId
        notificationsHandler.notifierName = commenterName
        notificationsHandler.notifierImageUrl = commenterImageUrl
        notificationsHandler.notifiedId = postPublisherId
        notificationsHandler.notifiedToken = commenterToken

        commentEditText.requestFocus()

        updateCommentsUI()

        sendCommentImageView.setOnClickListener {
            val commentContent = commentEditText.text.toString()

            if (commentContent.isEmpty() && commentData == null) {
                Toast.makeText(requireContext(), "Add comment first", Toast.LENGTH_SHORT).show()
            }

            else {
                if (commentData != null) {
                    mediaCommentLayoutPreview.visibility = View.VISIBLE
                    if (commentDataType == "image") {
                        var bitmap: Bitmap? = null
                        if (bitmapFromCamera) {
                            bitmap = commentData?.extras?.get("data") as Bitmap
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(
                                activity?.contentResolver,
                                commentData!!.data
                            )
                        }
                      mediaCommentPreviewImage.setImageBitmap(bitmap)
//                    postAtachmentImageView.setImageBitmap(bitmap)
                        progressDialog =
                            Utils.showProgressDialog(requireContext(), "Please wait...")
                        postViewModel.uploadImageCommentToCloudStorage(bitmap!!)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                                        commentAttachmentUrl = photoUrl.toString()

                                        val textComment = commentEditText.text.toString()
                                        val comment = if (textComment.isEmpty()) {
                                            createComment(commentAttachmentUrl!!, null, "image",superCommentId = this.comment.id.toString())
                                        } else {
                                            createComment(
                                                commentAttachmentUrl!!,
                                                commentContent,
                                                "textWithImage",
                                                superCommentId = this.comment.id.toString()
                                            )
                                        }
//                                        change 1

                                        postViewModel.addSubCommentToCommentById(
                                            this.comment.commenterId.toString(),
                                            this.comment.id.toString(),
                                            comment
                                        ).addOnCompleteListener { task ->
                                            commentEditText.text.clear()
                                            progressDialog?.dismiss()
                                            postViewModel
                                                .addCommentIdToCommentsCollection(
                                                    comment.commenterId.toString(),
                                                    comment.id.toString()
                                                )
                                            if (task.isSuccessful) {
                                                //if you are not the commenter
                                                mediaCommentLayoutPreview.visibility = View.GONE
                                                if (commenterId != postPublisherId) {
//                                                    comBottomSheetListener.onAnotherUserCommented(
//                                                        commentsList.size - 1,
//                                                        comment.id!!,
//                                                        postId
//                                                    )
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "I have to notify the user",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                updateCommentsUI()

                                            } else {
                                                Utils.toastMessage(
                                                    requireContext(),
                                                    task.exception?.message.toString()
                                                )

                                                Toast.makeText(
                                                    requireContext(),
                                                    "FAWZY",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        // dismiss()
                                    }
                                }
                            }
                    }
                    else if (commentDataType == "video") {
                        val videoUri = commentData!!.data!!
                        progressDialog =
                            Utils.showProgressDialog(requireContext(), "Please wait...")
                        postViewModel.uploadVideoCommentToCloudStorage(videoUri)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    task.result?.storage?.downloadUrl?.addOnSuccessListener { videoUrl ->
                                        commentAttachmentUrl = videoUrl.toString()

                                        val textComment = commentEditText.text.toString()
                                        val comment = if (textComment.isEmpty()) {
                                            createComment(commentAttachmentUrl!!, null, "video", superCommentId = this.comment.id.toString())
                                        } else {
                                            createComment(
                                                commentAttachmentUrl!!,
                                                commentContent,
                                                "textWithVideo",
                                                superCommentId = this.comment.id.toString()
                                            )
                                        }
                                        //change 2
                                        postViewModel.addSubCommentToCommentById(
                                            this.comment.commenterId.toString(),
                                            this.comment.id.toString(),
                                            comment
                                        ).addOnCompleteListener { task ->
                                            commentEditText.text.clear()
                                            progressDialog?.dismiss()
                                            postViewModel
                                                .addCommentIdToCommentsCollection(
                                                    comment.commenterId.toString(),
                                                    comment.id.toString()
                                                )
                                            if (task.isSuccessful) {
                                                //if you are not the commenter
                                                mediaCommentLayoutPreview.visibility = View.GONE
                                                if (commenterId != postPublisherId) {
//                                                    comBottomSheetListener.onAnotherUserCommented(
//                                                        commentsList.size - 1,
//                                                        comment.id!!,
//                                                        postId
//                                                    )
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "I have to notify the user",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                updateCommentsUI()
                                            } else {
                                                Utils.toastMessage(
                                                    requireContext(),
                                                    task.exception?.message.toString()
                                                )

                                                Toast.makeText(
                                                    requireContext(),
                                                    "FAWZY",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        //  dismiss()
                                    }
                                }
                            }
                    }
                }

                else {
//                    mediaCommentLayoutPreview.visibility = View.GONE
                    val comment =
                        createComment(
                            commentContent = commentContent,
                            commentType = "text",
                            attachmentCommentUrl = null,
                            superCommentId = this.comment.id.toString()
                        )

                    commentEditText.text.clear()

                    postViewModel.addSubCommentToCommentById(
                        this.comment.commenterId.toString(),
                        this.comment.id.toString(),
                        comment
                    )
                        .addOnCompleteListener {
                            //ADD COMMENT ID TO COMMENTS COLLECTION
                            //NOTIFICATION
                            postViewModel
                                .addCommentIdToCommentsCollection(
                                    comment.commenterId.toString(),
                                    comment.id.toString()
                                )
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        //if you are not the commenter
                                        //commenterId
                                        //NOTIFICATIONS
                                    if (comment.commenterId != postPublisherId) {
//                                        comBottomSheetListener.onAnotherUserCommented(
//                                            0,//temp
//                                            comment.id!!,
//                                            postId
//                                        )
                                        Toast.makeText(
                                            requireContext(),
                                            "I have no notify the user who commented",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    } else {
                                        Utils.toastMessage(
                                            requireContext(),
                                            task.exception?.message.toString()
                                        )

                                        Toast.makeText(
                                            requireContext(),
                                            "FAWZY",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                        }
                }
            }
                updateCommentsUI()
        }

        cancelMediaComment.setOnClickListener {
            mediaCommentLayoutPreview.visibility = View.GONE
            commentData = null
        }

        addAttachmentToComment.setOnClickListener {
            val addToPostBottomSheet = AddToPostBottomSheet(this)
            addToPostBottomSheet.show(activity?.supportFragmentManager!!, addToPostBottomSheet.tag)
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

        constraintLayout.setOnLongClickListener {
//            Toast.makeText(requireContext(), "Remove super comment", Toast.LENGTH_SHORT).show()
//            this.onCommentLongClicked(comment,)
            true
        }

        reactOnCommentTextView.setOnClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false

            postViewModel.getCommentById(postPublisherId, comment.id.toString())
                .addOnCompleteListener {
                    val commentDoc =
                        it.result?.toObject(ReactionsAndSubComments::class.java)
                    if (commentDoc?.reactions != null){
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
                    }
                    else{
                        clicksConsumer.reactOnCommentFromRepliesDataProvider(
                            comment,
                            commentPosition,
                            false,
                            null,
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
                    if (commentDoc?.reactions != null) {
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
                    }
                    else{
                        clicksConsumer.reactOnCommentFromRepliesDataProvider(
                            comment,
                            commentPosition,
                            false,
                            null,
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
        val longClickedCommentBottomSheet =
            LongClickedCommentBottomSheet(comment, postId, postPublisherId, "subComment")
        longClickedCommentBottomSheet.show(activity?.supportFragmentManager!!, "signature")
    }

    override fun onReactOnCommentClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        //I did not react
        if (!reacted) {
            val myReact = createReact(commenterId, commenterName, commenterImageUrl, 1)
            addReactOnComment(
                postPublisherId,
                comment.id.toString(),
                myReact
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    if (comment.commenterId  != commenterId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.reactType = 1
                            it.postId = postId
                            it.handleNotificationCreationAndFiring()
                        }

                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                    Toast.makeText(requireContext(), "ESLAM", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //If you have reacted --> delete react
        else {
            deleteReactFromComment(
                postPublisherId,
                comment.id.toString(),
                currentReact
            ).addOnCompleteListener { task ->
                updateCommentsUI()
                if (!task.isSuccessful) {
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                    Toast.makeText(requireContext(), "ALAA", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onReactOnCommentLongClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        showReactsChooserDialog(
            commenterId,//Interactor
            commenterName,
            commenterImageUrl,
            postId,
            postPublisherId,
            comment.id.toString(),
            comment.commenterId.toString(),
            currentReact,
            commentPosition
        )
    }

    override fun onReplyToCommentClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        commentEditText.requestFocus()
        Toast.makeText(requireContext(), "Focused reply", Toast.LENGTH_SHORT).show()
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
        commentType: String,
        superCommentId: String?
    ): Comment {
        return Comment(
            commenterId = commenterId,
            commenterName = commenterName,
            commenterImageUrl = commenterImageUrl,
            textComment = commentContent,
            commentType = commentType,
            attachmentCommentUrl = attachmentCommentUrl,
            superCommentId = superCommentId
        )
    }


    private fun createReact(
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reactType: Int?
    ): React {
        return React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl,
            react = reactType
        )
    }

    private fun addReactOnComment(
        postPublisherId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return postViewModel.addReactToReactsListInCommentDocument(
            postPublisherId,
            commentId,
            react
        )

    }

    private fun deleteReactFromComment(
        postPublisherId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return postViewModel.removeReactFromReactsListInCommentDocument(
            postPublisherId,
            commentId,
            react
        )
    }

    private fun showReactsChooserDialog(
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postId: String,
        postPublisherId: String,
        commentId: String,
        commenterId: String,
        currentReact: React?,
        commentPosition: Int
    ) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.long_clicked_reacts_button)
        val react = React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl
        )
        dialog.loveReactButton.setOnClickListener {
            react.react = 2
            if (currentReact != null) {
                deleteReactFromComment(postPublisherId, commentId, currentReact)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,2)
            addReactOnComment(postPublisherId, commentId, react).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    Log.i(TAG, "YYYY showReactsChooserDialog: $commenterId")
                    Log.i(TAG, "YYYY showReactsChooserDialog: $interactorId")
                    if (commenterId != interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.postId = postId
                            it.reactType = 2
                            it.handleNotificationCreationAndFiring()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            if (currentReact != null) {
                deleteReactFromComment(postPublisherId, commentId, currentReact)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,3)
            addReactOnComment(postPublisherId, commentId, react).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    if (commenterId != interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.postId = postId
                            it.reactType = 3
                            it.handleNotificationCreationAndFiring()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            if (currentReact != null) {
                deleteReactFromComment(postPublisherId, commentId, currentReact)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,4)
            addReactOnComment(postPublisherId, commentId, react).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    if (commenterId != interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.postId = postId
                            it.reactType = 4
                            it.handleNotificationCreationAndFiring()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            if (currentReact != null) {
                deleteReactFromComment(postPublisherId, commentId, currentReact)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,5)
            addReactOnComment(postPublisherId, commentId, react).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    if (commenterId != interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.postId = postId
                            it.reactType = 5
                            it.handleNotificationCreationAndFiring()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            if (currentReact != null) {
                deleteReactFromComment(postPublisherId, commentId, currentReact)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,6)
            addReactOnComment(postPublisherId, commentId, react).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    if (commenterId!= interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.postId = postId
                            it.reactType = 6
                            it.handleNotificationCreationAndFiring()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            if (currentReact != null) {
                deleteReactFromComment(postPublisherId, commentId, currentReact)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,7)
            addReactOnComment(postPublisherId, commentId, react).addOnCompleteListener { task ->
                updateCommentsUI() // to update
                if (task.isSuccessful) {
                    if (commenterId!= interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
                            it.commentPosition = commentPosition
                            it.postId = postId
                            it.reactType = 7
                            it.handleNotificationCreationAndFiring()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        dialog.show()

    }

    override fun onAttachmentAdded(data: Intent?, dataType: String, fromCamera: Boolean) {
        if (data != null) {
            commentData = data
            commentDataType = dataType
            bitmapFromCamera = fromCamera

            if (commentData != null) {
            }
            mediaCommentLayoutPreview.visibility = View.VISIBLE
            if (commentDataType == "image") {
                var bitmap: Bitmap? = null
                if (bitmapFromCamera) {
                    bitmap = commentData?.extras?.get("data") as Bitmap
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(
                        activity?.contentResolver,
                        commentData!!.data
                    )
                }
                mediaCommentPreviewImage.setImageBitmap(bitmap)
            }
            else if (commentDataType == "video"){
//                val interval: Long = 1 * 1000
//                val options: RequestOptions = RequestOptions().frame(interval)
//                Glide.with(requireContext())
//                    .asBitmap().load(comment.attachmentCommentUrl).apply(options)
//                    .into(mediaCommentPreviewImage)

                Toast.makeText(requireContext(), "Video comment", Toast.LENGTH_SHORT).show()
            }


        }
        else{
            mediaCommentLayoutPreview.visibility = View.GONE
        }
    }


}
