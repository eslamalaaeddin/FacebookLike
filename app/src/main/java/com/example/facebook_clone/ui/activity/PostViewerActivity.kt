package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.CommentsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.listener.CommentClickListener
import com.example.facebook_clone.helper.listener.ReactClickListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.PostViewModel
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
import kotlinx.android.synthetic.main.profile_post_item.*
import kotlinx.android.synthetic.main.profile_post_item.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PostViewerActivity"

class PostViewerActivity : AppCompatActivity(), CommentClickListener, ReactClickListener {
    private val postViewModel by viewModel<PostViewModel>()
    private val picasso = Picasso.get()
    private var commentsAdapter: CommentsAdapter? = null
    private var post: Post? = null
    private val auth: FirebaseAuth by inject()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_viewer)
        //71951780018

        val postPublisherId = intent.getStringExtra("postPublisherId").toString()
        val postId = intent.getStringExtra("postId").toString()
        val commentPosition = intent.getIntExtra("commentPosition", 0)
        val publisherName = intent.getStringExtra("publisherName").toString()
        val publisherImageUrl = intent.getStringExtra("publisherImageUrl").toString()

        Toast.makeText(this, "$commentPosition", Toast.LENGTH_SHORT).show()

        /*
        POST LIVE DATA IS BETTER


        /////*
        /*
        */
        */
         */

        postViewModel.getPostById(postPublisherId, postId).addOnCompleteListener { task ->
            post = task.result?.toObject(Post::class.java)
            post?.let { post ->
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
                reactsCountTextView.text = post.reacts?.size.toString()

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

                if (post.comments != null ) {
                    commentsAdapter =
                        CommentsAdapter(
                            auth.currentUser?.uid.toString(),
                            post.comments!!,
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
                            //itemView.likeReactPlaceHolder.visibility = View.INVISIBLE
                        }
                    }
                }
            }


        }
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
            if (post != null) {
                //Image
                if (post?.attachmentUrl!!.contains("jpeg")) {
                    val imageViewerDialog = ImageViewerDialog()
                    imageViewerDialog.show(supportFragmentManager, "signature")
                    imageViewerDialog.setMediaUrl(post?.attachmentUrl!!)
                }
                //video(I chosed an activity to show media controllers)
                else {
                    val videoIntent = Intent(this, VideoPlayerActivity::class.java)
                    videoIntent.putExtra("videoUrl", post?.attachmentUrl)
                    startActivity(videoIntent)
                }
            }
        }

        upButtonImageView.setOnClickListener { finish() }

        addReactTextView.setOnClickListener {
            //if reacted --> remove react
            //else --> add react
        }

        addReactTextView.setOnLongClickListener {

            true
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

    override fun onReactButtonLongClicked() {
    }

    override fun onReactButtonClicked() {
    }

    override fun onReactButtonClicked(react: React?) {
    }
}