package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils.POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.PROFILE_POSTS_COLLECTION
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.helper.posthandler.PostViewerActivityPostsHandler
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.ui.bottomsheet.PeopleWhoReactedBottomSheet
import com.example.facebook_clone.ui.bottomsheet.PostConfigurationsBottomSheet
import com.example.facebook_clone.viewmodel.*
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
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "PostViewerActivity"

class PostViewerActivity : AppCompatActivity(), CommentsBottomSheetListener {
    private val postViewModel by viewModel<PostViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewerActivityViewModel by viewModel<PostViewerViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private lateinit var notificationsHandler: NotificationsHandler
    private val picasso = Picasso.get()
    private var post: Post = Post()
    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
    private lateinit var postPublisherToken: String
    private lateinit var interactorId: String
    private lateinit var interactorName: String
    private lateinit var interactorImageUrl: String
    private lateinit var postId: String
    private lateinit var postPublisherId: String
    private val currentUserId = auth.currentUser?.uid.toString()
    private lateinit var postViewerActivityPostsHandler: PostViewerActivityPostsHandler

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_viewer)

        notificationsHandler = NotificationsHandler(
            othersProfileActivityViewModel = othersProfileActivityViewModel,
            notificationsFragmentViewModel = notificationsFragmentViewModel
        )

        val postPublisherId = intent.getStringExtra("postPublisherId").toString()
        val postId = intent.getStringExtra("postId").toString()

        post.firstCollectionType = POSTS_COLLECTION
        post.creatorReferenceId = postPublisherId
        post.secondCollectionType = PROFILE_POSTS_COLLECTION
        post.id = postId

        val postLiveData = postViewerActivityViewModel.getPostLiveData(post)
        postLiveData?.observe(this, { post ->
            if (post != null) {
                this.post = post
                this.postId = post.id.toString()
                this.postPublisherId = post.publisherId.toString()

                notificationsHandler.postPublisherId = post.publisherId.toString()

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

                reactsCountTextView.text = post.reacts.orEmpty().size.toString()

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

                sharesCountTextView.text = "${post.shares.orEmpty().size.toString()} Shares"
                commentsCountsTextView.text = "${post.comments.orEmpty().size.toString()} Comments"

                post.reacts?.let { reacts ->
                    if (reacts.isEmpty()) {
                        reactImageViewGrey.visibility = View.VISIBLE
                        reactImageViewBlue.visibility = View.INVISIBLE
                        addReactTextView.text = "Like"
                        addReactTextView.setTextColor(resources.getColor(R.color.gray))
                    }
                    for (react in reacts) {
                        // || post.reacts?.isEmpty()!!
                        if (react.reactorId == auth.currentUser?.uid.toString()) {
                            reactImageViewGrey.visibility = View.INVISIBLE
                            reactImageViewBlue.visibility = View.VISIBLE
                            when (react.react) {
                                1 -> {handleReactsPositioning("Like", R.color.dark_blue, R.drawable.ic_thumb_up)}
                                2 -> {handleReactsPositioning("Love", R.color.red, R.drawable.ic_love_react)}
                                3 -> {handleReactsPositioning("Care", R.color.orange, R.drawable.ic_care_react)}
                                4 -> {handleReactsPositioning("Haha", R.color.orange, R.drawable.ic_haha_react)}
                                5 -> {handleReactsPositioning("Wow", R.color.orange, R.drawable.ic_wow_react)}
                                6 -> {handleReactsPositioning("Sad", R.color.yellow, R.drawable.ic_sad_react)}
                                7 -> {handleReactsPositioning("Angry", R.color.orange, R.drawable.ic_angry_angry)}
                            }
                            break
                        }
                        //no react from me
                        else {
                            reactImageViewGrey.visibility = View.VISIBLE
                            reactImageViewBlue.visibility = View.INVISIBLE
                            addReactTextView.text = "Like"
                            addReactTextView.setTextColor(resources.getColor(R.color.gray))
                        }
                    }
                }
            } else {
                //finish()
                Toast.makeText(this, postPublisherId, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, postId, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Post was deleted", Toast.LENGTH_SHORT).show()
            }

        })

        val postPublisherLiveData = profileActivityViewModel.getAnotherUser(postPublisherId)
        postPublisherLiveData?.observe(this, {
            notificationsHandler.notifiedToken = it.token
            postPublisherToken = it.token.toString()
            postViewerActivityPostsHandler = PostViewerActivityPostsHandler(
                this,
                postViewModel,
                profileActivityViewModel,
                notificationsFragmentViewModel,
                othersProfileActivityViewModel,
                postPublisherToken
            )
        })

        val myLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        myLiveData?.observe(this, { user ->
            currentUser = user
            notificationsHandler.notifierId = currentUser.id
            notificationsHandler.notifierName = currentUser.name
            notificationsHandler.notifierImageUrl = currentUser.profileImageUrl
            interactorId = user.id.toString()
            interactorName = user.name.toString()
            interactorImageUrl = user.profileImageUrl.toString()
        })

        circleImageView.setOnClickListener { navigateToPostPublisherProfile(postPublisherId) }

        userNameTextView.setOnClickListener { navigateToPostPublisherProfile(postPublisherId) }

        attachmentImageView.setOnClickListener {
            post.let { post -> postViewerActivityPostsHandler.handleMediaClicks(post.attachmentUrl.orEmpty()) }
        }

        upButtonImageView.setOnClickListener { finish() }

        addReactTextView.setOnClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false
            post.reacts?.let { reacts ->
                for (react in reacts) {
                    if (react.reactorId == interactorId) {
                        currentReact = react
                        reacted = true
                        break
                    }
                }
            }
            postViewerActivityPostsHandler.onReactTextViewClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact
            )
        }

        addReactTextView.setOnLongClickListener {
            var currentReact: React? = null
            var reacted: Boolean = false
            post.reacts?.let { reacts ->
                for (react in reacts) {
                    if (react.reactorId == interactorId) {
                        currentReact = react
                        reacted = true
                        break
                    }
                }
            }
            postViewerActivityPostsHandler.onReactTextViewLongClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact
            )
            true
        }

        addCommentOnPostTextView.setOnClickListener { openCommentsBottomSheet() }

        showCommentsTextView.setOnClickListener { openCommentsBottomSheet() }

        addShareOnPostTextView.setOnClickListener {
            postViewerActivityPostsHandler.onShareButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                -1
            )
        }

        postViewerReactsLayout.setOnClickListener { openPeopleWhoReactedLayout(null, null, "post") }

        moreOnPost.setOnClickListener {
            //but if it was not my post
            post?.let { post ->
                val postConfigurationsBottomSheet = PostConfigurationsBottomSheet(post, null)
                postConfigurationsBottomSheet.show(
                    supportFragmentManager,
                    postConfigurationsBottomSheet.tag
                )
            }
        }
    }

    private fun handleReactsPositioning(reactText: String, colorId: Int, imageId: Int) {
        addReactTextView.text = reactText
        addReactTextView.setTextColor(resources.getColor(colorId))
        reactImageViewBlue.setImageResource(imageId)
    }

    private fun openCommentsBottomSheet() {
        postViewerActivityPostsHandler.onShowCommentsClicked(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            -1,
            postPublisherToken
        )
    }

    private fun navigateToPostPublisherProfile(postPublisherId: String) {
        if (postPublisherId == auth.currentUser?.uid.toString()) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = Intent(this, OthersProfileActivity::class.java)
            intent.putExtra("userId", postPublisherId)
            startActivity(intent)
        }
    }

    override fun onAnotherUserCommented(commentPosition: Int, commentId: String, postId: String) {
        notificationsHandler.also {
            it.notifiedId = postPublisherId
            it.notificationType = "commentOnPost"
            it.postId = postId
            it.commentPosition = commentPosition
            it.handleNotificationCreationAndFiring()
        }

    }

    private fun openPeopleWhoReactedLayout(commenterId: String?, commentId: String?, reactedOn: String) {
        val peopleWhoReactedDialog = PeopleWhoReactedBottomSheet(
                commenterId.toString(),
                commentId.toString(),
                post,
                reactedOn
            )
        peopleWhoReactedDialog.show(
            supportFragmentManager,
            peopleWhoReactedDialog.tag
        )
    }

}