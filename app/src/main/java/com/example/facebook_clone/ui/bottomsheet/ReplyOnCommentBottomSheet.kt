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
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.activity.NewsFeedActivity
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
    private val post: Post,
    private val clicksConsumer: ReplyOnCommentDataProvider,
    private val postPublisherId: String,
    private val superComment: Comment,
    private val commentPosition: Int,
    private val interactorId: String,
    private val interactorName: String,
    private val interactorImageUrl: String,
    //private val commenterToken: String,//the person whom you are replying to (super comment owner)
    private val postId: String,
    private val reacted: Boolean,
    private val currentReact: React?
) : BottomSheetDialogFragment(),
    CommentClickListener,
    ReactClickListener,
    PostAttachmentListener {
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
    private lateinit var commentSubComments: List<Comment>
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

        notificationsHandler.notifierId = interactorId
        notificationsHandler.notifierName = interactorName
        notificationsHandler.notifierImageUrl = interactorImageUrl
        notificationsHandler.postPublisherId = postPublisherId
//        notificationsHandler.notifiedToken = commenterToken
        notificationsHandler.firstCollectionType = post.firstCollectionType
        notificationsHandler.creatorReferenceId = post.creatorReferenceId
        notificationsHandler.secondCollectionType = post.secondCollectionType

        commentEditText.requestFocus()

        updateCommentsUI()

        sendCommentImageView.setOnClickListener {
            val commentContent = commentEditText.text.toString()

            if (commentContent.isEmpty() && commentData == null) {
                Toast.makeText(requireContext(), "Add comment first", Toast.LENGTH_SHORT).show()
            } else {
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
                                            createComment(
                                                commentAttachmentUrl!!,
                                                null,
                                                "image",
                                                superCommentId = this.superComment.id.toString()
                                            )
                                        } else {
                                            createComment(
                                                commentAttachmentUrl!!,
                                                commentContent,
                                                "textWithImage",
                                                superCommentId = this.superComment.id.toString()
                                            )
                                        }
//                                        change 1

                                        postViewModel.addSubCommentToCommentById(
                                            this.superComment.commenterId.toString(),
                                            this.superComment.id.toString(),
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
                                                commentData = null

                                                if (this.superComment.commenterId.toString() != interactorId) {
                                                    val commenterToBeNotifiedLiveData =
                                                        notificationsFragmentViewModel.getUserLiveData(this.superComment.commenterId.toString())
                                                    commenterToBeNotifiedLiveData?.observe(viewLifecycleOwner) { user ->
                                                        val token = user.token
                                                        notificationsHandler.also {
                                                            it.notifiedId =
                                                                this.superComment.commenterId.toString()
                                                            it.notificationType = "commentOnComment"
                                                            it.postId = postId
                                                            it.notifiedToken = token
//                                                        it.commentPosition = commentPosition
                                                            it.handleNotificationCreationAndFiring()
                                                            commenterToBeNotifiedLiveData.removeObservers(viewLifecycleOwner)
                                                        }
                                                    }

                                                }

//                                                var tempComment = ""
//                                                for (subComment in commentSubComments) {
//                                                    if (subComment.commenterId != this.superComment.commenterId.toString() && subComment.commenterId != tempComment) {
//                                                        tempComment =
//                                                            subComment.commenterId.toString()
//                                                        notificationsHandler.also {
//                                                            it.notifiedId = subComment.commenterId
//                                                            it.notifiedToken =
//                                                                subComment.commenterToken
//                                                            it.notificationType = "commentOnComment"
//                                                            it.postId = postId
////                                                            it.commentPosition = commentPosition
//                                                            it.handleNotificationCreationAndFiring()
//                                                        }
//                                                    }
//                                                }
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
                                            createComment(
                                                commentAttachmentUrl!!,
                                                null,
                                                "video",
                                                superCommentId = this.superComment.id.toString()
                                            )
                                        } else {
                                            createComment(
                                                commentAttachmentUrl!!,
                                                commentContent,
                                                "textWithVideo",
                                                superCommentId = this.superComment.id.toString()
                                            )
                                        }
                                        //change 2
                                        postViewModel.addSubCommentToCommentById(
                                            this.superComment.commenterId.toString(),
                                            this.superComment.id.toString(),
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
                                                commentData = null

                                                if (this.superComment.commenterId.toString() != interactorId) {
                                                    val commenterToBeNotifiedLiveData =
                                                        notificationsFragmentViewModel.getUserLiveData(this.superComment.commenterId.toString())
                                                    commenterToBeNotifiedLiveData?.observe(viewLifecycleOwner) { user ->
                                                        val token = user.token
                                                        notificationsHandler.also {
                                                            it.notifiedId =
                                                                this.superComment.commenterId.toString()
                                                            it.notificationType = "commentOnComment"
                                                            it.postId = postId
                                                            it.notifiedToken = token
                                                            it.handleNotificationCreationAndFiring()
                                                            commenterToBeNotifiedLiveData.removeObservers(viewLifecycleOwner)
                                                        }
                                                    }
                                                }

                                                var tempComment = ""
                                                for (subComment in commentSubComments) {
                                                    if (subComment.commenterId != this.superComment.commenterId.toString() && subComment.commenterId != tempComment) {
                                                        tempComment =
                                                            subComment.commenterId.toString()
                                                        notificationsHandler.also {
                                                            it.notifiedId = subComment.commenterId
                                                            it.notifiedToken =
                                                                subComment.commenterToken
                                                            it.notificationType = "commentOnComment"
                                                            it.postId = postId
//                                                            it.commentPosition = commentPosition
                                                            it.handleNotificationCreationAndFiring()
                                                        }
                                                    }
                                                }
                                                updateCommentsUI()
                                            } else {
                                                Utils.toastMessage(
                                                    requireContext(),
                                                    task.exception?.message.toString()
                                                )
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
                            superCommentId = this.superComment.id.toString()
                        )

                    commentEditText.text.clear()

                    postViewModel.addSubCommentToCommentById(
                        this.superComment.commenterId.toString(),
                        this.superComment.id.toString(),
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
                                        if (this.superComment.commenterId.toString() != interactorId) {
                                            val commenterToBeNotifiedLiveData =
                                                notificationsFragmentViewModel.getUserLiveData(this.superComment.commenterId.toString())
                                            commenterToBeNotifiedLiveData?.observe(viewLifecycleOwner) { user ->
                                                val token = user.token
                                                notificationsHandler.also {
                                                    it.notifiedId =
                                                        this.superComment.commenterId.toString()
                                                    it.notificationType = "commentOnComment"
                                                    it.postId = postId
                                                    it.notifiedToken = token
                                                    it.handleNotificationCreationAndFiring()
                                                    commenterToBeNotifiedLiveData.removeObservers(viewLifecycleOwner)
                                                }
                                            }
                                        }
                                        //to notify all commeners
//                                        var tempComment = ""
//                                        for (subComment in commentSubComments) {
//                                            if (subComment.commenterId != this.superComment.commenterId.toString() && subComment.commenterId != tempComment) {
//                                                tempComment = subComment.commenterId.toString()
//                                                notificationsHandler.also {
//                                                    it.notifiedId = subComment.commenterId
//                                                    it.notifiedToken = subComment.commenterToken
//                                                    it.notificationType = "commentOnComment"
//                                                    it.postId = postId
////                                                    it.commentPosition = commentPosition
//                                                    it.handleNotificationCreationAndFiring()
//                                                }
//                                            }
//                                        }
                                    } else {
                                        Utils.toastMessage(
                                            requireContext(),
                                            task.exception?.message.toString()
                                        )
                                    }
                                }

                        }
                }
            }
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
            val mediaUrl = superComment.attachmentCommentUrl.toString()
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
            val longClickedCommentBottomSheet =
                LongClickedCommentBottomSheet(null, superComment, post, "comment")
            longClickedCommentBottomSheet.show(activity?.supportFragmentManager!!, "signature")
            true
        }

        reactOnCommentTextView.setOnClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false

            postViewModel.getCommentById(
                superComment.commenterId.toString(),
                superComment.id.toString()
            )
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
                                superComment,
                                commentPosition,
                                reacted,
                                currentReact,
                                "click"
                            )
                        }
                    } else {
                        clicksConsumer.reactOnCommentFromRepliesDataProvider(
                            superComment,
                            commentPosition,
                            false,
                            null,
                            "click"
                        )
                    }
                }

        }

        reactOnCommentTextView.setOnLongClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false

            postViewModel.getCommentById(
                superComment.commenterId.toString(),
                superComment.id.toString()
            )
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
                                superComment,
                                commentPosition,
                                reacted,
                                currentReact,
                                "longClick"
                            )
                        }
                    } else {
                        clicksConsumer.reactOnCommentFromRepliesDataProvider(
                            superComment,
                            commentPosition,
                            false,
                            null,
                            "longClick"
                        )
                    }
                }
            true
        }

        replyOnCommentTextView.setOnClickListener { commentEditText.requestFocus() }

        whoReactedOnCommentLayout.setOnClickListener {
            openPeopleWhoReactedLayout(
                superComment.commenterId.toString(),
                superComment.id.toString(),
                "comment"
            )
        }
    }


    private fun updateCommentsUI() {
        postViewModel.getCommentUpdates(
            superComment.commenterId.toString(),
            superComment.id.toString()
        )?.addSnapshotListener { value, error ->
            val reactionsAndComments = value?.toObject(ReactionsAndSubComments::class.java)

            //if there is not document
            if (reactionsAndComments == null) {
                dismiss()
            }
            reactionsAndComments?.reactions?.let { reacts ->
                if (reacts.isNotEmpty()) {
                    if (reactsCountTextView != null) {
                        reactsCountTextView.text = reacts.size.toString()
                        reactsCountTextView.visibility = View.VISIBLE
                    }
                    for (react in reacts) {
                        if (react.reactorId == auth.currentUser?.uid.toString()) {
                            if (myReactPlaceHolder != null) {
                                myReactPlaceHolder.visibility = View.VISIBLE
                            }

                            when (react.react) {
                                1 -> {
                                    if (reactOnCommentTextView != null) {
                                        reactOnCommentTextView.text = "Like"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.dark_blue
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_like_react)
                                    }
                                }
                                2 -> {
                                    if (reactOnCommentTextView != null) {
                                        reactOnCommentTextView.text = "Love"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.red
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_love_react)
                                    }
                                }
                                3 -> {
                                    if (reactOnCommentTextView != null) {
                                        reactOnCommentTextView.text = "Care"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_care_react)
                                    }
                                }
                                4 -> {
                                    if (reactOnCommentTextView != null) {
                                        reactOnCommentTextView.text = "Haha"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_haha_react)
                                    }
                                }
                                5 -> {
                                    if (reactOnCommentTextView != null) {
                                        reactOnCommentTextView.text = "Wow"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_wow_react)
                                    }
                                }
                                6 ->{
                                    if (reactOnCommentTextView != null) {
                                            reactOnCommentTextView.text = "Sad"
                                            reactOnCommentTextView.setTextColor(
                                                activity?.resources?.getColor(
                                                    R.color.yellow
                                                )!!
                                            )
                                            myReactPlaceHolder.setImageResource(R.drawable.ic_sad_react)
                                        }
                                    }
                                7 -> {
                                    if (reactOnCommentTextView != null) {
                                        reactOnCommentTextView.text = "Angry"
                                        reactOnCommentTextView.setTextColor(
                                            activity?.resources?.getColor(
                                                R.color.orange
                                            )!!
                                        )
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_angry_angry)
                                    }
                                }
                            }

                            break
                        } else {
                            //Visible to me
                            myReactPlaceHolder.visibility = View.INVISIBLE
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
                } else {
                    if (myReactPlaceHolder != null) {
                        myReactPlaceHolder.visibility = View.INVISIBLE
                    }

                    if (reactsCountTextView != null) {
                        reactsCountTextView.visibility = View.INVISIBLE
                    }
                    if (reactsCountTextView != null) {
                        reactOnCommentTextView.text = "Like"
                        reactOnCommentTextView.setTextColor(
                            activity?.resources?.getColor(
                                R.color.gray
                            )!!
                        )
                    }
                }

            }

            reactionsAndComments?.let {
//                    if (it.subComments != null){
                commentSubComments = reactionsAndComments.subComments.orEmpty()
                commentsAdapter = CommentsAdapter(
                    auth.currentUser?.uid.toString(),
                    reactionsAndComments.subComments.orEmpty(),
                    null,
                    this,
                    this,
                    postViewModel,
                    postPublisherId
                )
                if (subCommentsRecyclerView != null) {
                    subCommentsRecyclerView.adapter = commentsAdapter
                }
            }
        }

        picasso.load(superComment.commenterImageUrl).into(commenterImageView)
        commenterNameTextView.text = superComment.commenterName
        commentCreationTimeTextView.text =
            DateFormat.format("EEE, MMM d, h:mm a", superComment.commentTime.toDate())

        val interval: Long = 1 * 1000
        val options: RequestOptions = RequestOptions().frame(interval)

        if (superComment.commentType == "text") {
            commentTextView.text = superComment.textComment
            mediaCommentCardView.visibility = View.GONE
        } else if (superComment.commentType == "textWithImage") {
            commentTextView.text = superComment.textComment
            picasso.load(superComment.attachmentCommentUrl).into(mediaCommentImageView)
            mediaCommentCardView.visibility = View.VISIBLE
        } else if (superComment.commentType == "textWithVideo") {
            commentTextView.text = superComment.textComment
            Glide.with(requireContext())
                .asBitmap().load(superComment.attachmentCommentUrl).apply(options)
                .into(mediaCommentImageView)
            mediaCommentCardView.visibility = View.VISIBLE
        } else if (superComment.commentType == "image") {
            picasso.load(superComment.attachmentCommentUrl).into(mediaCommentImageView)
            mediaCommentCardView.visibility = View.VISIBLE
        } else if (superComment.commentType == "video") {
            Glide.with(requireContext())
                .asBitmap().load(superComment.attachmentCommentUrl).apply(options)
                .into(mediaCommentImageView)
            mediaCommentCardView.visibility = View.VISIBLE
        }

    }

    override fun onCommentLongClicked(comment: Comment) {
        val longClickedCommentBottomSheet =
            LongClickedCommentBottomSheet(superComment, comment, post, "subComment")
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
            val myReact = createReact(interactorId, interactorName, interactorImageUrl, 1)
            addReactOnComment(
                comment.commenterId.toString(),
                comment.id.toString(),
                myReact
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    Log.i(TAG, "TOOT onReactOnCommentClicked: ${comment.commenterId.toString()}")
                    Log.i(TAG, "TOOT onReactOnCommentClicked: $interactorId")
                    notificationsHandler.notifiedId = comment.commenterId.toString()
                    if (comment.commenterId.toString() != interactorId) {
                        notificationsHandler.also {
                            it.notificationType = "reactOnComment"
//                            it.commentPosition = commentPosition
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
                comment.commenterId.toString(),
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
            interactorId,//Interactor
            interactorName,
            interactorImageUrl,
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

    override fun onCommentReactionsLayoutClicked(commenterId: String, commentId: String) {
        openPeopleWhoReactedLayout(commenterId, commentId, "comment")
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
            commenterId = interactorId,
            commenterName = interactorName,
            commenterImageUrl = interactorImageUrl,
            textComment = commentContent,
            commentType = commentType,
            attachmentCommentUrl = attachmentCommentUrl,
            superCommentId = superCommentId,
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
        commenterId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return postViewModel.addReactToReactsListInCommentDocument(
            commenterId,
            commentId,
            react
        )

    }

    private fun deleteReactFromComment(
        commenterId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return postViewModel.removeReactFromReactsListInCommentDocument(
            commenterId,
            commentId,
            react
        )
    }

    private fun handleLongReactOnCommentCreationAndDeletion(
        currentReact: React?,
        react: React,
        commentId: String,
        postPublisherId: String,
        commenterId: String,
        commentPosition: Int
    ) {
        if (currentReact != null) {
            deleteReactFromComment(commenterId, commentId, currentReact)
        }
        addReactOnComment(commenterId, commentId, react).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                notificationsHandler.notifiedId = commenterId
                Log.i(TAG, "TOOT handleLongReactOnCommentCreationAndDeletion: $commenterId")
                if (commenterId != interactorId) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnComment"
//                        it.commentPosition = commentPosition
                        it.postId = postId
                        it.reactType = react.react
                        it.handleNotificationCreationAndFiring()
                    }
                }
            } else {
                Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
            updateCommentsUI()
        }
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
            handleLongReactOnCommentCreationAndDeletion(
                currentReact,
                react,
                commentId,
                postPublisherId,
                commenterId,
                commentPosition
            )
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            handleLongReactOnCommentCreationAndDeletion(
                currentReact,
                react,
                commentId,
                postPublisherId,
                commenterId,
                commentPosition
            )
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            handleLongReactOnCommentCreationAndDeletion(
                currentReact,
                react,
                commentId,
                postPublisherId,
                commenterId,
                commentPosition
            )
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            handleLongReactOnCommentCreationAndDeletion(
                currentReact,
                react,
                commentId,
                postPublisherId,
                commenterId,
                commentPosition
            )
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            handleLongReactOnCommentCreationAndDeletion(
                currentReact,
                react,
                commentId,
                postPublisherId,
                commenterId,
                commentPosition
            )
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            handleLongReactOnCommentCreationAndDeletion(
                currentReact,
                react,
                commentId,
                postPublisherId,
                commenterId,
                commentPosition
            )
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
            } else if (commentDataType == "video") {
//                val interval: Long = 1 * 1000
//                val options: RequestOptions = RequestOptions().frame(interval)
//                Glide.with(requireContext())
//                    .asBitmap().load(comment.attachmentCommentUrl).apply(options)
//                    .into(mediaCommentPreviewImage)

                Toast.makeText(requireContext(), "Video comment", Toast.LENGTH_SHORT).show()
            }


        } else {
            mediaCommentLayoutPreview.visibility = View.GONE
        }
    }

    private fun openPeopleWhoReactedLayout(
        commenterId: String?,
        commentId: String?,
        reactedOn: String
    ) {
        val peopleWhoReactedDialog =
            PeopleWhoReactedBottomSheet(
                commenterId.toString(),
                commentId.toString(),
                post,
                reactedOn
            )
        peopleWhoReactedDialog.show(
            activity?.supportFragmentManager!!,
            peopleWhoReactedDialog.tag
        )
    }


}
