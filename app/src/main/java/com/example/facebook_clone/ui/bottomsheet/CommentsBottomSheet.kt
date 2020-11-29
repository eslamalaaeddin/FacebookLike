package com.example.facebook_clone.ui.bottomsheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.PostAttachmentListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.helper.provider.ReplyOnCommentDataProvider
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.comment.CommentDocument
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.react.ReactDocument
import com.example.facebook_clone.model.post.share.Share
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
import kotlinx.android.synthetic.main.comments_bottom_sheet.*
import kotlinx.android.synthetic.main.comments_bottom_sheet.addAttachmentToComment
import kotlinx.android.synthetic.main.comments_bottom_sheet.commentEditText
import kotlinx.android.synthetic.main.comments_bottom_sheet.myReactPlaceHolder
import kotlinx.android.synthetic.main.comments_bottom_sheet.sendCommentImageView
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "CommentsBottomSheet"

class CommentsBottomSheet(
    private val postPublisherId: String,
    private val postId: String,
    private val interactorId: String,
    private val interactorName: String,
    private val interactorImageUrl: String,
    private val commentsBottomSheetListener: CommentsBottomSheetListener?,
    private val postPublisherToken: String
) : BottomSheetDialogFragment(),
    CommentClickListener,
    ReactClickListener,
    PostAttachmentListener,
    ReplyOnCommentDataProvider {
    private val postViewModel by viewModel<PostViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private lateinit var comBottomSheetListener: CommentsBottomSheetListener
    private lateinit var commentsList: List<Comment>
    private lateinit var reactsList: List<React>
    private val auth: FirebaseAuth by inject()
    private lateinit var commentsAdapter: CommentsAdapter
    private var commentData: Intent? = null
    private var commentDataType: String? = null
    private var bitmapFromCamera: Boolean = false
    private var commentAttachmentUrl: String? = null
    private lateinit var notificationsHandler: NotificationsHandler
    private var progressDialog: ProgressDialog? = null
    private lateinit var commentFromReplyBottomSheet: Comment
    private var commentPositionFromReplyBottomSheet = 0
    private var reactedFromReplyBottomSheet: Boolean = false
    private var currentReactFromReplyBottomSheet: React? = null
    private var reactClicked = false
    //private lateinit var comments: List<Comment>

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
        if (commentsBottomSheetListener != null) {
            comBottomSheetListener = commentsBottomSheetListener
        }
        return layoutInflater.inflate(R.layout.comments_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentEditText.requestFocus()

        notificationsHandler = NotificationsHandler(
            othersProfileActivityViewModel = othersProfileActivityViewModel,
            notificationsFragmentViewModel = notificationsFragmentViewModel
        )

        notificationsHandler.notifierId = interactorId
        notificationsHandler.notifierName = interactorName
        notificationsHandler.notifierImageUrl = interactorImageUrl
        notificationsHandler.notifiedId = postPublisherId
        notificationsHandler.notifiedToken = postPublisherToken


        reactorsLayout.setOnClickListener {
            openPeopleWhoReactedLayout(null, "post")
        }



        updateCommentsUI()


        sendCommentImageView.setOnClickListener {
            val commentContent = commentEditText.text.toString()

            if (commentContent.isEmpty() && commentData == null) {
                Toast.makeText(requireContext(), "Add comment first", Toast.LENGTH_SHORT).show()
            }

            else {
                if (commentData != null) {
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
                        mediaCommentPreviewImg.setImageBitmap(bitmap)
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
                                            createComment(commentAttachmentUrl!!, null, "image")
                                        } else {
                                            createComment(
                                                commentAttachmentUrl!!,
                                                commentContent,
                                                "textWithImage"
                                            )
                                        }
//                                        change 1

                                        postViewModel.addCommentToPostComments(
                                            postId,
                                            postPublisherId,
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
                                                mediaCommentLayoutPrev.visibility = View.GONE
                                                commentData = null
                                                if (interactorId != postPublisherId) {
                                                    comBottomSheetListener.onAnotherUserCommented(
                                                        commentsList.size - 1,
                                                        comment.id!!,
                                                        postId
                                                    )
                                                }
                                                commentData = null
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
                                            createComment(commentAttachmentUrl!!, null, "video")
                                        } else {
                                            createComment(
                                                commentAttachmentUrl!!,
                                                commentContent,
                                                "textWithVideo"
                                            )
                                        }
                                        //change 2
                                        postViewModel.addCommentToPostComments(
                                            postId,
                                            postPublisherId,
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
                                                mediaCommentLayoutPrev.visibility = View.GONE
                                                commentData = null
                                                if (interactorId != postPublisherId) {
                                                    comBottomSheetListener.onAnotherUserCommented(
                                                        commentsList.size - 1,
                                                        comment.id!!,
                                                        postId
                                                    )
                                                }
                                                commentData = null
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
                //Text comment
                else {
                    val comment =
                        createComment(
                            commentContent = commentContent,
                            commentType = "text",
                            attachmentCommentUrl = null
                        )

                    commentEditText.text.clear()

                    postViewModel.addCommentToPostComments(postId, postPublisherId, comment)
                        .addOnCompleteListener { task ->
                            //   progressDialog?.dismiss()
                            postViewModel
                                .addCommentIdToCommentsCollection(
                                    comment.commenterId.toString(),
                                    comment.id.toString()
                                )
                            if (task.isSuccessful) {
                                //if you are not the commenter
                                //commenterId
                                if (comment.commenterId != postPublisherId) {
                                    comBottomSheetListener.onAnotherUserCommented(
                                        commentsList.size - 1,
                                        comment.id!!,
                                        postId
                                    )
                                }
                            } else {
                                Utils.toastMessage(
                                    requireContext(),
                                    task.exception?.message.toString()
                                )

                                Toast.makeText(requireContext(), "FAWZY", Toast.LENGTH_SHORT).show()
                            }
                            //dismiss()
                        }
                }
            }
        }

        addAttachmentToComment.setOnClickListener {
            val addToPostBottomSheet = AddToPostBottomSheet(this)
            addToPostBottomSheet.show(activity?.supportFragmentManager!!, addToPostBottomSheet.tag)
        }

        dismissMediaComment.setOnClickListener {
            mediaCommentLayoutPrev.visibility = View.GONE
            commentData = null
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateCommentsUI() {
        postViewModel.getPostById(postPublisherId, postId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.reference?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Utils.toastMessage(requireContext(), error.message.toString())
                        return@addSnapshotListener
                    }
                    val post = snapshot?.toObject(Post::class.java)
                    val commentsResult = snapshot?.toObject(CommentDocument::class.java)?.comments
                    val reactsResult = snapshot?.toObject(ReactDocument::class.java)?.reacts

                    commentsList = commentsResult.orEmpty().reversed()
                    reactsList = reactsResult.orEmpty().reversed()


                    for (react in reactsList) {
                        if (react.reactorId == interactorId) {
                            if (myReactPlaceHolder != null) {
                                when (react.react) {

                                    1 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_like_react)
                                    }
                                    2 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_love_react)
                                    }
                                    3 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_care_react)
                                    }
                                    4 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_haha_react)
                                    }
                                    5 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_wow_react)
                                    }
                                    6 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_sad_react)
                                    }
                                    7 -> {
                                        myReactPlaceHolder.setImageResource(R.drawable.ic_angry_angry)
                                    }
                                }
                                break
                            }
                        }
                    }

                    commentsAdapter =
                        CommentsAdapter(
                            interactorId,
                            commentsList,
                            reactsList,
                            this,
                            this,
                            postViewModel,
                            postPublisherId
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
            LongClickedCommentBottomSheet(null, comment, postId, postPublisherId, "comment")
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
                    if (comment.commenterId.toString() != interactorId && interactorId != auth.currentUser?.uid.toString()) {
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

    private fun onReactOnCommentClickedFromReplies(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        //I did not react
        if (!reacted) {
            val myReact = createReact(interactorId, interactorName, interactorImageUrl, 1)
            addReactOnComment(
                comment.commenterId.toString(),//this comments is the super comment so :(
                comment.id.toString(),
                myReact
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCommentsUI()
                    if (comment.commenterId != interactorId) {
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
        Log.i(TAG, "FAWZY onReactOnCommentLongClicked: $currentReact")
        showReactsChooserDialog(
            interactorId = auth.currentUser?.uid.toString(),
            interactorName = interactorName,
            interactorImageUrl = interactorImageUrl,
            postId = postId,
            postPublisherId = postPublisherId,
            commentId = comment.id.toString(),
            commenterId = comment.commenterId.toString(),
            currentReact = currentReact,
            commentPosition = commentPosition
        )
    }

    override fun onReplyToCommentClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {
        val replyOnCommentBottomSheet = ReplyOnCommentBottomSheet(
            this,
            postPublisherId,
            comment,
            commentPosition,
            interactorId,
            interactorName,
            interactorImageUrl,
            "ff9jw4VpQB6UGgmw8pb7yv:APA91bF9nQk-5BcheHo_kPwPkLDHw9kZ1_4TuSZ4wOV9bHKW9GJMM1ZBh5bBFZiIBUeegM9zg6mx9ngUnBXJhITv4mjAsYkQNl9gr7GyU7QoxeHGWOy1Jd8uwBWe5LD5VSzj4G-mjxNz",
            postId,
            reacted,
            currentReact
        )
        replyOnCommentBottomSheet.show(
            activity?.supportFragmentManager!!,
            replyOnCommentBottomSheet.tag
        )
    }


    override fun onCommentReactionsLayoutClicked(commentId: String) {
        openPeopleWhoReactedLayout(commentId, "comment")
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

    //adding react
    override fun onReactButtonClicked() {
        // createReact()
    }

    //removing react
    override fun onReactButtonClicked(react: React?) {
        // deleteReact(react!!)
    }

    private fun createComment(
        attachmentCommentUrl: String?,
        commentContent: String?,
        commentType: String
    ): Comment {
        return Comment(
            commenterId = interactorId,
            commenterName = interactorName,
            commenterImageUrl = interactorImageUrl,
            textComment = commentContent,
            commentType = commentType,
            attachmentCommentUrl = attachmentCommentUrl
        )
    }

    override fun onAttachmentAdded(data: Intent?, dataType: String, fromCamera: Boolean) {
        if (data != null) {
            commentData = data
            commentDataType = dataType
            bitmapFromCamera = fromCamera

            if (commentData != null) {
            }
            mediaCommentLayoutPrev.visibility = View.VISIBLE
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
                mediaCommentPreviewImg.setImageBitmap(bitmap)
            } else if (commentDataType == "video") {
//                val interval: Long = 1 * 1000
//                val options: RequestOptions = RequestOptions().frame(interval)
//                Glide.with(requireContext())
//                    .asBitmap().load(comment.attachmentCommentUrl).apply(options)
//                    .into(mediaCommentPreviewImage)

                Toast.makeText(requireContext(), "Video comment", Toast.LENGTH_SHORT).show()
            }


        } else {
            mediaCommentLayoutPrev.visibility = View.GONE
        }


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

    private fun createShare(share: Share, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.addShareToPost(share, postId, postPublisherId)
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
            deleteReactFromComment(commenterId, commentId,currentReact )
        }
        addReactOnComment(commenterId, commentId, react).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateCommentsUI()
                if (commenterId != interactorId  && interactorId != auth.currentUser?.uid.toString()) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnComment"
                        it.commentPosition = commentPosition
                        it.postId = postId
                        it.reactType = react.react
                        it.handleNotificationCreationAndFiring()
                    }
                }
            } else {
                Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
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
            handleLongReactOnCommentCreationAndDeletion(currentReact, react, commentId, postPublisherId, commenterId, commentPosition)
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            handleLongReactOnCommentCreationAndDeletion(currentReact, react, commentId, postPublisherId, commenterId, commentPosition)
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            handleLongReactOnCommentCreationAndDeletion(currentReact, react, commentId, postPublisherId, commenterId, commentPosition)
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            handleLongReactOnCommentCreationAndDeletion(currentReact, react, commentId, postPublisherId, commenterId, commentPosition)
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            handleLongReactOnCommentCreationAndDeletion(currentReact, react, commentId, postPublisherId, commenterId, commentPosition)
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            handleLongReactOnCommentCreationAndDeletion(currentReact, react, commentId, postPublisherId, commenterId, commentPosition)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun openPeopleWhoReactedLayout(commentId: String?, reactedOn: String) {
        val peopleWhoReactedDialog =
            PeopleWhoReactedBottomSheet(commentId.toString(), postId, postPublisherId, reactedOn)
        peopleWhoReactedDialog.show(
            activity?.supportFragmentManager!!,
            peopleWhoReactedDialog.tag
        )
    }

    override fun reactOnCommentFromRepliesDataProvider(
        superComment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?,
        clickType: String
    ) {
        if (clickType == "click") {
            onReactOnCommentClickedFromReplies(superComment, commentPosition, reacted, currentReact)
            Toast.makeText(requireContext(), "Click", Toast.LENGTH_SHORT).show()
        }
        else if (clickType == "longClick") {
            showReactsChooserDialog(
                interactorId,//Interactor
                interactorName,
                interactorImageUrl,
                postId,
                postPublisherId,
                superComment.id.toString(),
                superComment.commenterId.toString(),
                currentReact,
                commentPosition
            )
            Toast.makeText(requireContext(), "LongClick", Toast.LENGTH_SHORT).show()

        }
    }


}
