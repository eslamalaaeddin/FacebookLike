package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
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
import com.example.facebook_clone.viewmodel.PostViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_viewer.*
import kotlinx.android.synthetic.main.profile_post_item.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PostViewerActivity"
class PostViewerActivity : AppCompatActivity(), CommentClickListener, ReactClickListener {
    private val postViewModel by viewModel<PostViewModel>()
    private val picasso = Picasso.get()
    private var commentsAdapter : CommentsAdapter? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_viewer)
        //71951780018

        val postPublisherId = intent.getStringExtra("postPublisherId").toString()
        val postId = intent.getStringExtra("postId").toString()
        val publisherName = intent.getStringExtra("publisherName").toString()
        val publisherImageUrl = intent.getStringExtra("publisherImageUrl").toString()

        picasso.load(publisherImageUrl).into(circleImageView)
        userNameTextView.text = publisherName

        postViewModel.getPostById(postPublisherId, postId).addOnCompleteListener {task ->
            val post = task.result?.toObject(Post::class.java)
            postContentTextView.text = post?.content
            postTimeTextView.text =
                DateFormat.format("EEEE, MMM d, yyyy h:mm a", post?.creationTime?.toDate())
            //visibility
            val visibility = post?.visibility
            if (visibility == 0) {
                postVisibilityImageView.setImageResource(R.drawable.ic_public_visibility)
            } else if (visibility == 1) {
                postVisibilityImageView.setImageResource(R.drawable.ic_friends_visibility)
            } else {
                postVisibilityImageView.setImageResource(R.drawable.ic_private_visibility)
            }
            reactsCountTextView.text = post?.reacts?.size.toString()

            if (post?.attachmentUrl != null){
                attachmentImageView.visibility = View.VISIBLE
                if (post.attachmentType == "image"){
                    picasso.load(post.attachmentUrl).into(attachmentImageView)
                    playButtonImageView.visibility = View.GONE
                }
                else if(post.attachmentType == "video"){
                    playButtonImageView.visibility = View.VISIBLE
                    val interval: Long = 1* 1000
                    val options: RequestOptions = RequestOptions().frame(interval)
                    Glide.with(this)
                        .asBitmap().load(post.attachmentUrl).apply(options).into(attachmentImageView)
                }
            }else{
                 attachmentImageView.visibility = View.GONE
                 playButtonImageView.visibility = View.GONE
            }

            //Shares count
            if (post?.shares != null && post.shares!!.isNotEmpty()) {
                sharesCountTextView.text = "${post.shares?.size.toString()} Shares"
            }
            //Comments count
            if (post?.comments != null && post.comments!!.isNotEmpty()) {
                commentsCountsTextView.text = "${post.comments?.size.toString()} Comments"
            }

            if (post?.comments != null && post.reacts != null) {
                commentsAdapter =
                    CommentsAdapter(post.comments!!, post.reacts!!, this, this)

                postViewerCommentsRecyclerView.adapter = commentsAdapter
            }
        }

        upButtonImageView.setOnClickListener {
            finish()
        }
    }

    override fun onCommentLongClicked(comment: Comment) {
    }

//    override fun onReactOnCommentClicked(commentId: String, commentPosition: Int) {
//
//    }

    override fun onMediaCommentClicked(mediaUrl: String) {

    }

    override fun onReactButtonLongClicked() {
    }

    override fun onReactButtonClicked() {
    }

    override fun onReactButtonClicked(react: React?) {
    }
}