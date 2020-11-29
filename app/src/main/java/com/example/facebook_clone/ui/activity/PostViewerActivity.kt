package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.PostConfigurationsBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.PostViewerViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_viewer.*
import kotlinx.android.synthetic.main.activity_post_viewer.addReactTextView
import kotlinx.android.synthetic.main.activity_post_viewer.attachmentImageView
import kotlinx.android.synthetic.main.activity_post_viewer.circleImageView
import kotlinx.android.synthetic.main.activity_post_viewer.commentsCountsTextView
import kotlinx.android.synthetic.main.activity_post_viewer.playButtonImageView
import kotlinx.android.synthetic.main.activity_post_viewer.postContentTextView
import kotlinx.android.synthetic.main.activity_post_viewer.postTimeTextView
import kotlinx.android.synthetic.main.activity_post_viewer.postVisibilityImageView
import kotlinx.android.synthetic.main.activity_post_viewer.reactImageViewBlue
import kotlinx.android.synthetic.main.activity_post_viewer.reactImageViewGrey
import kotlinx.android.synthetic.main.activity_post_viewer.reactsCountTextView
import kotlinx.android.synthetic.main.activity_post_viewer.sharesCountTextView
import kotlinx.android.synthetic.main.activity_post_viewer.userNameTextView
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PostViewerActivity"

class PostViewerActivity : AppCompatActivity(), CommentClickListener, ReactClickListener {
    private val postViewModel by viewModel<PostViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewerActivityViewModel by viewModel<PostViewerViewModel>()
    private val picasso = Picasso.get()
    private var commentsAdapter: CommentsAdapter? = null
    private var post: Post? = null
    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
    private lateinit var interactorId: String
    private lateinit var interactorName: String
    private lateinit var interactorImageUrl: String
    private lateinit var postId: String
    private lateinit var postPublisherId: String
    private val currentUserId = auth.currentUser?.uid.toString()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_viewer)
        //71951780018

        val postPublisherId = intent.getStringExtra("postPublisherId").toString()
        val postId = intent.getStringExtra("postId").toString()
        val commentPosition = intent.getIntExtra("commentPosition", -1)
        val publisherName = intent.getStringExtra("publisherName").toString()
        val publisherImageUrl = intent.getStringExtra("publisherImageUrl").toString()

        val postLiveData = postViewerActivityViewModel.getPostLiveData(postPublisherId, postId)
        postLiveData?.observe(this, { post ->
            if (post != null) {
                this.post = post
                this.postId = post.id.toString()
                this.postPublisherId = post.publisherId.toString()

                picasso.load(post.publisherImageUrl).into(circleImageView)
                userNameTextView.text = post.publisherName
                postContentTextView.text = post.content
                postTimeTextView.text =
                    DateFormat.format("EEEE, MMM d, yyyy h:mm a", post.creationTime.toDate())
                //visibility
                val visibility = post.visibility
                if (visibility == 0) {
                    postVisibilityImageView.setImageResource(R.drawable.ic_public_visibility)
                } else if (visibility == 1) {
                    postVisibilityImageView.setImageResource(R.drawable.ic_friends_visibility)
                } else {
                    postVisibilityImageView.setImageResource(R.drawable.ic_private_visibility)
                }

                if (post?.reacts?.isNotEmpty()!!) {
                    reactsCountTextView.text = post.reacts?.size.toString()
                }

                if (post.attachmentUrl != null) {
                    attachmentImageView.visibility = View.VISIBLE
                    if (post.attachmentType == "image") {
                        picasso.load(post.attachmentUrl).into(attachmentImageView)
                        playButtonImageView.visibility = View.GONE
                    } else if (post.attachmentType == "video") {
                        playButtonImageView.visibility = View.VISIBLE
                        val interval: Long = 1 * 1000
                        val options: RequestOptions = RequestOptions().frame(interval)
                        Glide.with(this)
                            .asBitmap().load(post.attachmentUrl).apply(options)
                            .into(attachmentImageView)
                    }
                } else {
                    attachmentImageView.visibility = View.GONE
                    playButtonImageView.visibility = View.GONE
                }

                //Shares count
                if (post.shares != null && post.shares!!.isNotEmpty()) {
                    sharesCountTextView.text = "${post.shares?.size.toString()} Shares"
                }
                //Comments count
                if (post.comments != null && post.comments!!.isNotEmpty()) {
                    commentsCountsTextView.text = "${post.comments?.size.toString()} Comments"
                }

                if (post.comments != null) {
                    commentsAdapter =
                        CommentsAdapter(
                            auth.currentUser?.uid.toString(),
                            post.comments!!.reversed(),
                            post.reacts.orEmpty(),
                            this,
                            this,
                            postViewModel,
                            postPublisherId
                        )

                    postViewerCommentsRecyclerView.adapter = commentsAdapter
                    postViewerCommentsRecyclerView.scrollToPosition(commentPosition)
                }

                post.reacts?.let { reacts ->
                    if (reacts.isEmpty()) {
                        reactImageViewGrey.visibility = View.VISIBLE
                        reactImageViewBlue.visibility = View.INVISIBLE
                        addReactTextView.text = "Like"
                        addReactTextView.setTextColor(
                            resources.getColor(
                                R.color.gray
                            )
                        )
                    }
                    for (react in reacts) {
                        // || post.reacts?.isEmpty()!!
                        if (react.reactorId == postPublisherId) {
                            reactImageViewGrey.visibility = View.INVISIBLE
                            reactImageViewBlue.visibility = View.VISIBLE
                            when (react.react) {
                                1 -> {
                                    addReactTextView.text = "Like"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.dark_blue
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_thumb_up)
                                }
                                2 -> {
                                    addReactTextView.text = "Love"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.red
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_love_react)
                                }
                                3 -> {
                                    addReactTextView.text = "Care"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.orange
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_care_react)
                                }
                                4 -> {
                                    addReactTextView.text = "Haha"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.orange
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_haha_react)
                                }
                                5 -> {
                                    addReactTextView.text = "Wow"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.orange
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_wow_react)
                                }
                                6 -> {
                                    addReactTextView.text = "Sad"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.yellow
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_sad_react)
                                }
                                7 -> {
                                    addReactTextView.text = "Angry"
                                    addReactTextView.setTextColor(
                                        resources.getColor(
                                            R.color.orange
                                        )
                                    )
                                    reactImageViewBlue.setImageResource(R.drawable.ic_angry_angry)
                                }
                            }
                            break
                        }
                        //no react from me
                        else {
                            reactImageViewGrey.visibility = View.VISIBLE
                            reactImageViewBlue.visibility = View.INVISIBLE
                            addReactTextView.text = "Like"
                            addReactTextView.setTextColor(
                                resources.getColor(
                                    R.color.gray
                                )
                            )
                            //itemView.likeReactPlaceHolder.visibility = View.INVISIBLE
                        }
                    }
                }
            } else {
                finish()
                Toast.makeText(this, "Post was deleted", Toast.LENGTH_SHORT).show()
            }

        })

        val userLiveDate = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        userLiveDate?.observe(this, { user ->
            currentUser = user
            interactorId = user.id.toString()
            interactorName = user.name.toString()
            interactorImageUrl = user.profileImageUrl.toString()
        })

        circleImageView.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }

        userNameTextView.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }

        attachmentImageView.setOnClickListener {
            post?.let { post ->
                if (post.attachmentUrl!!.contains("jpeg")) {
                    val imageViewerDialog = ImageViewerDialog()
                    imageViewerDialog.show(supportFragmentManager, "signature")
                    imageViewerDialog.setMediaUrl(post.attachmentUrl!!)
                } else {
                    val videoIntent = Intent(this, VideoPlayerActivity::class.java)
                    videoIntent.putExtra("videoUrl", post.attachmentUrl)
                    startActivity(videoIntent)
                }
            }
        }

        upButtonImageView.setOnClickListener { finish() }

        addReactTextView.setOnClickListener {
            val postReacts = post?.reacts.orEmpty()
            if (postPublisherId == currentUserId) {
                if (postReacts.isEmpty()) {
                    val myReact = createReact(interactorId, interactorName, interactorImageUrl)
                    addReactOnPostToDb(
                        myReact,
                        postId,
                        postPublisherId
                    ).addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    for ((index, react) in postReacts.withIndex()) {
                        //I have Reacted
                        if (react.reactorId == currentUserId) {
                            Log.i(TAG, "YOYO onCreate: Remove React")
                            deleteReactFromPost(
                                react,
                                postId,
                                postPublisherId
                            ).addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Utils.toastMessage(this, task.exception?.message.toString())
                                }
                            }
                            break
                        }
                        //I have not reacted and i am in the last react in the list
                        else if (react.reactorId != currentUserId && index == postReacts.size - 1) {
                            Log.i(TAG, "YOYO onCreate: Add React")
                            val myReact =
                                createReact(interactorId, interactorName, interactorImageUrl)
                            addReactOnPostToDb(
                                myReact,
                                postId,
                                postPublisherId
                            ).addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        task.exception?.message,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }

                        } else if (postReacts.isEmpty()) {
                            val myReact =
                                createReact(interactorId, interactorName, interactorImageUrl)
                            addReactOnPostToDb(
                                myReact,
                                postId,
                                postPublisherId
                            ).addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        task.exception?.message,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        }
                    }
                }
            }
            else{
                Toast.makeText(this, "Notify the post publisher", Toast.LENGTH_SHORT).show()
            }
        }

        addReactTextView.setOnLongClickListener {
            val postReacts = post?.reacts.orEmpty()
            if (postPublisherId == currentUserId) {
                if (postReacts.isEmpty()) {
                    showReactsChooserDialog(
                        interactorId,
                        interactorName,
                        interactorImageUrl,
                        postId,
                        postPublisherId,
                        null
                    )
                } else {
                    for ((index, react) in postReacts.withIndex()) {
                        //I have Reacted
                        if (react.reactorId == currentUserId) {
                            showReactsChooserDialog(
                                interactorId,
                                interactorName,
                                interactorImageUrl,
                                postId,
                                postPublisherId,
                                react
                            )
                            break
                        }
                        //I have not reacted and i am in the last react in the list
                        else if (react.reactorId != currentUserId && index == postReacts.size - 1) {
                            showReactsChooserDialog(
                                interactorId,
                                interactorName,
                                interactorImageUrl,
                                postId,
                                postPublisherId,
                                null
                            )
                        }


                    }
                }
            }
            else{
                Toast.makeText(this, "Notify the post publisher", Toast.LENGTH_SHORT).show()
            }
            true
        }

        addCommentOnPostTextView.setOnClickListener {
//            commentEditText.requestFocus()
            CommentsBottomSheet(
                postPublisherId,
                postId,
                interactorId,
                interactorName,
                interactorImageUrl,
                null,//used to handle notification so, no need for it in my profile.
                currentUser.token.toString()
            ).apply {
                show(supportFragmentManager, tag)
            }
        }

        postViewerReactsLayout.setOnClickListener {
            CommentsBottomSheet(
                postPublisherId,
                postId,
                interactorId,
                interactorName,
                interactorImageUrl,
                null,
                currentUser.token.toString(),
            ).apply {
                show(supportFragmentManager, tag)
            }
        }

        addShareOnPostTextView.setOnClickListener {
            if (post?.publisherId == currentUserId) {
                val share = Share(
                    sharerId = interactorId,
                    sharerName = interactorName,
                    sharerImageUrl = interactorImageUrl,
                )
                addShareToPost(share, postId, postPublisherId).addOnCompleteListener { task ->
                    Utils.doAfterFinishing(this, task, "You shared this post")
                }
            } else {
                Toast.makeText(this, "Share with notify", Toast.LENGTH_SHORT).show()
            }
        }

        moreOnPost.setOnClickListener {
            post?.let { post ->
                val postConfigurationsBottomSheet = PostConfigurationsBottomSheet(post)
                postConfigurationsBottomSheet.show(
                    supportFragmentManager,
                    postConfigurationsBottomSheet.tag
                )
            }
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

    }

    override fun onReactOnCommentLongClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {

    }

    override fun onReplyToCommentClicked(
        comment: Comment,
        commentPosition: Int,
        reacted: Boolean,
        currentReact: React?
    ) {

    }

    override fun onCommentReactionsLayoutClicked(commentId: String) {

    }

    override fun onMediaCommentClicked(mediaUrl: String) {
        //Image
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show(supportFragmentManager, "signature")
            imageViewerDialog.setMediaUrl(mediaUrl)
        }
        //video(I chosed an activity to show media controllers)
        else {
            val videoIntent = Intent(this, VideoPlayerActivity::class.java)
            videoIntent.putExtra("videoUrl", mediaUrl)
            startActivity(videoIntent)
        }
    }

    //////////////////////////////////////////COMMENTS REACT CLICK LISTENER///////////////////////////////////

    override fun onReactButtonLongClicked() {
    }

    override fun onReactButtonClicked() {
    }

    override fun onReactButtonClicked(react: React?) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun handlePostReactsAdditionAndDeletion(reacted: Boolean, currentReact: React?) {
        if (!reacted) {
            val myReact = createReact(interactorId, interactorName, interactorImageUrl)
            addReactOnPostToDb(myReact, postId, postPublisherId).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            deleteReactFromPost(
                currentReact!!,
                postId,
                postPublisherId
            ).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
        }
    }

    private fun addReactOnPostToDb(
        react: React,
        postId: String,
        postPublisherId: String
    ): Task<Void> {
        return postViewModel.addReactToDB(react, postId, postPublisherId)
    }

    private fun createReact(
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    ): React {
        return React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl
        )
    }

    private fun deleteReactFromPost(
        react: React,
        postId: String,
        postPublisherId: String
    ): Task<Void> {
        return postViewModel.deleteReactFromPost(react, postId, postPublisherId)
    }

    private fun addShareToPost(share: Share, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.addShareToPost(share, postId, postPublisherId)
    }

    private fun showReactsChooserDialog(
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postId: String,
        postPublisherId: String,
        currentReact: React?
    ) {
        val dialog = Dialog(this)
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
            handleLongReactCreationAndDeletion(currentReact, react, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            handleLongReactCreationAndDeletion(currentReact, react, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            handleLongReactCreationAndDeletion(currentReact, react, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            handleLongReactCreationAndDeletion(currentReact, react, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            handleLongReactCreationAndDeletion(currentReact, react, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            handleLongReactCreationAndDeletion(currentReact, react, postId, postPublisherId)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun handleLongReactCreationAndDeletion(
        currentReact: React?,
        react: React,
        postId: String,
        postPublisherId: String
    ) {
        if (currentReact != null) {
            deleteReactFromPost(currentReact, postId, postPublisherId)
        }
        addReactOnPostToDb(react, postId, postPublisherId)
    }

}