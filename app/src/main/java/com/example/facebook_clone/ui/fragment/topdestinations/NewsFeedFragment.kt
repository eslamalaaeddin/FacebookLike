package com.example.facebook_clone.ui.fragment.topdestinations

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.Utils.POST_FROM_PROFILE
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.activity.PostViewerViewModel
import com.example.facebook_clone.viewmodel.activity.ProfileActivityViewModel
import com.example.facebook_clone.viewmodel.fragment.NewsFeedFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_feed_news.*
import kotlinx.android.synthetic.main.fragment_feed_news.smallUserImageView
import kotlinx.android.synthetic.main.fragment_feed_news.whatIsInYourMindButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsFeedFragment : Fragment(R.layout.fragment_feed_news), PostListener{
    private val newsFeedFragmentViewModel by viewModel<NewsFeedFragmentViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private val currentUserId = auth.currentUser?.uid.toString()
    private var currentUser = User()
    private val picasso = Picasso.get()
    private var profilePostsAdapter: ProfilePostsAdapter? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserLiveData = profileActivityViewModel.getMe(currentUserId)
        currentUserLiveData?.observe(viewLifecycleOwner){user ->
            currentUser = user
            picasso.load(user.profileImageUrl).into(smallUserImageView)
        }

        val newsFeedPostLiveData = postViewModel.getUserNewsFeedPostsLiveData(currentUserId)
        newsFeedPostLiveData.observe(viewLifecycleOwner){posts ->
            updateNewsFeedPosts(posts.orEmpty())
        }

        whatIsInYourMindButton.setOnClickListener {
            val postCreatorDialog = PostCreatorDialog(POST_FROM_PROFILE, currentUser = currentUser)
            postCreatorDialog.show(activity?.supportFragmentManager!!, "signature")
            postCreatorDialog.setUserNameAndProfileImageUrl(
                currentUser.name.toString(),
                currentUser.profileImageUrl.toString()
            )
        }
    }

    private fun updateNewsFeedPosts(posts: List<Post>) {
        if(profilePostsAdapter == null){
            profilePostsAdapter = ProfilePostsAdapter(
                auth,
                posts,
                this,
                currentUser.name.toString(),
                currentUser.profileImageUrl.toString(),
                true,
                currentUser.id.toString()
            )
        }
        else{
            profilePostsAdapter?.let {
                it.setPosts(posts)
                it.notifyDataSetChanged()
            }
        }
        postsRecyclerView.adapter = profilePostsAdapter
    }

    override fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String?
    ) {

    }

    override fun onReactButtonLongClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String?
    ) {
    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String?
    ) {
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
    }

    override fun onMediaPostClicked(mediaUrl: String) {
    }

    override fun onPostMoreDotsClicked(interactorId: String, post: Post, shared: Boolean?) {
    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {
    }
}