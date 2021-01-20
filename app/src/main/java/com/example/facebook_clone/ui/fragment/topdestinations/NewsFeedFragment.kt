package com.example.facebook_clone.ui.fragment.topdestinations

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.NewsFeedFragmentPostsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.Utils.POST_FROM_GROUP
import com.example.facebook_clone.helper.Utils.POST_FROM_PAGE
import com.example.facebook_clone.helper.Utils.POST_FROM_PROFILE
import com.example.facebook_clone.helper.listener.NewsFeedPostListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.posthandler.BasePostHandler
import com.example.facebook_clone.helper.posthandler.OthersProfileActivityPostsHandler
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.ui.activity.*
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.activity.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.activity.PostViewerViewModel
import com.example.facebook_clone.viewmodel.activity.ProfileActivityViewModel
import com.example.facebook_clone.viewmodel.fragment.NewsFeedFragmentViewModel
import com.example.facebook_clone.viewmodel.fragment.NotificationsFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_feed_news.*
import kotlinx.android.synthetic.main.fragment_feed_news.smallUserImageView
import kotlinx.android.synthetic.main.fragment_feed_news.whatIsInYourMindButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsFeedFragment : Fragment(R.layout.fragment_feed_news), NewsFeedPostListener {
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private lateinit var basePostHandler: BasePostHandler
    private val currentUserId = auth.currentUser?.uid.toString()
    private var currentUser = User()
    private val picasso = Picasso.get()
    private lateinit var currentPosts : List<Post>
    private lateinit var newsFeedFragmentPostsAdapter: NewsFeedFragmentPostsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        basePostHandler = BasePostHandler(requireContext(), postViewModel)
        val currentUserLiveData = profileActivityViewModel.getMe(currentUserId)
        currentUserLiveData?.observe(viewLifecycleOwner) { user ->
            picasso.load(user.profileImageUrl).into(smallUserImageView)
            currentUser = user
            val newsFeedPostLiveData = postViewModel.getUserNewsFeedPostsLiveData(currentUserId)
            newsFeedPostLiveData.observe(viewLifecycleOwner) { posts ->
                currentPosts = posts
                updateNewsFeedPosts(posts.orEmpty())
            }

        }

        val itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.RIGHT -> navigateToPostViewerActivity(currentPosts[viewHolder.adapterPosition])
                    }
                }

                //We can implement it manually or use xabaras library
                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            })

        itemTouchHelper.attachToRecyclerView(postsRecyclerView)

        whatIsInYourMindButton.setOnClickListener {
            val postCreatorDialog = PostCreatorDialog(POST_FROM_PROFILE, currentUser = currentUser)
            postCreatorDialog.show(activity?.supportFragmentManager!!, "signature")
            postCreatorDialog.setUserNameAndProfileImageUrl(
                currentUser.name.toString(),
                currentUser.profileImageUrl.toString()
            )
        }
    }

    private fun navigateToPostViewerActivity(post: Post) {
        val postPublisherId = post.publisherId
        val postId = post.id
        val firstCollectionType = post.firstCollectionType
        val creatorReferenceId = post.creatorReferenceId
        val secondCollectionType = post.secondCollectionType
        val fromWhere = post.fromWhere
        val groupId = post.groupId

        val intent = Intent(requireContext(), PostViewerActivity::class.java)
        intent.putExtra("postPublisherId", postPublisherId)
        intent.putExtra("postId", postId)
        intent.putExtra("firstCollectionType", firstCollectionType)
        intent.putExtra("creatorReferenceId", creatorReferenceId)
        intent.putExtra("secondCollectionType", secondCollectionType)
        intent.putExtra("fromWhere", fromWhere)
        intent.putExtra("groupId", groupId)

        startActivity(intent)

    }

    private fun updateNewsFeedPosts(posts: List<Post>) {
        newsFeedFragmentPostsAdapter = NewsFeedFragmentPostsAdapter(posts, this)
        postsRecyclerView.adapter = newsFeedFragmentPostsAdapter
    }

    override fun onUserImageClicked(post: Post) {
        navigateToPostPublisherProfile(post.publisherId.orEmpty())
    }

    override fun onUserNameClicked(post: Post) {
        navigateToPostPublisherProfile(post.publisherId.orEmpty())
    }

    override fun onGroupOrPageNameClicked(post: Post) {
        if (post.fromWhere == POST_FROM_GROUP){
            navigateToGroupActivity(post.groupId.orEmpty())
        }
        else if (post.fromWhere == POST_FROM_PAGE){
//            navigateToPageActivity(post.pageId.orEmpty)
        }
    }

    private fun navigateToPageActivity(pageId: String) {
        val intent = Intent(requireContext(), PageActivity::class.java)
        intent.putExtra("pageId", pageId)
        startActivity(intent)
    }

    private fun navigateToGroupActivity(groupId: String) {
        val intent = Intent(requireContext(), GroupActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
    }

    override fun onMediaClicked(mediaUrl: String) {
        basePostHandler.handleMediaClicks(mediaUrl)
    }

    private fun navigateToPostPublisherProfile(postPublisherId: String) {
        if (postPublisherId == auth.currentUser?.uid.toString()) {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        } else {
            val intent = Intent(requireContext(), OthersProfileActivity::class.java)
            intent.putExtra("userId", postPublisherId)
            startActivity(intent)
        }
    }

}