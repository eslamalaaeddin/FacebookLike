package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.FriendsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.posthandler.ProfileActivityPostsHandler
import com.example.facebook_clone.helper.Utils.POST_FROM_PROFILE
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.ui.bottomsheet.ProfileCoverBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileImageBottomSheet
import com.example.facebook_clone.ui.bottomsheet.UserFriendsBottomSheet
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bioTextView
import kotlinx.android.synthetic.main.activity_profile.coverImageView
import kotlinx.android.synthetic.main.activity_profile.friendsRecyclerView
import kotlinx.android.synthetic.main.activity_profile.joinDateTextView
import kotlinx.android.synthetic.main.activity_profile.profileImageView
import kotlinx.android.synthetic.main.activity_profile.profilePostsRecyclerView
import kotlinx.android.synthetic.main.activity_profile.smallUserImageView
import kotlinx.android.synthetic.main.activity_profile.userNameTextView
import kotlinx.android.synthetic.main.activity_profile.whatIsInYourMindButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "ProfileActivity"
private const val REQUEST_CODE_COVER_IMAGE = 123
private const val REQUEST_CODE_PROFILE_IMAGE = 456

class ProfileActivity() : AppCompatActivity(), FriendClickListener{
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private lateinit var  profileActivityPostsHandler : ProfileActivityPostsHandler
    private lateinit var currentUser: User
    private lateinit var picasso: Picasso
    private var profilePostsAdapter: ProfilePostsAdapter? = null
    private lateinit var friendsAdapter: FriendsAdapter
    private var currentEditedPostPosition: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileActivityPostsHandler = ProfileActivityPostsHandler("ProfileActivity", this, postViewModel, profileActivityViewModel)
        picasso = Picasso.get()

        val userLiveDate = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        userLiveDate?.observe(this, { user ->
            user?.let {
                currentUser = user
                updateUserIdentity(user)
                updateUserFriends(user.friends.orEmpty())
                //To get current user i have nested the livedata
                val postsLiveData =
                    postViewModel.getUserProfilePostsLiveData(auth.currentUser?.uid.toString())
                postsLiveData.observe(this, { posts ->
                    updateUserPosts(posts)
                })
            }
        })

        profileImageView.setOnClickListener {
            ProfileImageBottomSheet(currentUser.profileImageUrl.toString()).apply {
                show(supportFragmentManager, tag)
            }
        }

        coverImageView.setOnClickListener {
            ProfileCoverBottomSheet(currentUser.coverImageUrl.toString()).apply {
                show(supportFragmentManager, tag)
            }
        }

        coverCameraImageView.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE_COVER_IMAGE)
            }
        }

        takePhotoForProfileImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE_PROFILE_IMAGE)
            }
        }

        whatIsInYourMindButton.setOnClickListener {
            val postCreatorDialog = PostCreatorDialog(POST_FROM_PROFILE)
            postCreatorDialog.show(supportFragmentManager, "signature")
            postCreatorDialog.setUserNameAndProfileImageUrl(
                    currentUser.name.toString(),
                    currentUser.profileImageUrl.toString()
                )
        }

        seeAllFriendsButton.setOnClickListener {
            showUserFriendsBottomSheet()
        }
    }

    private fun showUserFriendsBottomSheet() {
        val userFriendsBottomSheet = UserFriendsBottomSheet(friends = currentUser.friends.orEmpty(), this)
        userFriendsBottomSheet.show(supportFragmentManager, userFriendsBottomSheet.tag)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserIdentity(user: User) {
        if (user.coverImageUrl != null) {
            picasso.load(user.coverImageUrl).into(coverImageView)
        }
        if (user.profileImageUrl != null) {
            picasso.load(user.profileImageUrl).into(profileImageView)
            picasso.load(user.profileImageUrl).into(smallUserImageView)
        }
        if (user.biography != null) {
            bioTextView.text = user.biography
        }
        val date = android.text.format.DateFormat.format("MMM, yyyy", user.creationTime.toDate())

        userNameTextView.text = user.name
        joinDateTextView.text = "Joined $date"

    }

    private fun updateUserPosts(posts: List<Post>) {
        if(profilePostsAdapter == null){
            profilePostsAdapter = ProfilePostsAdapter(
                auth,
                posts,
                profileActivityPostsHandler,
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
        profilePostsRecyclerView.adapter = profilePostsAdapter
        if (currentEditedPostPosition != -1) {
            profilePostsRecyclerView.scrollToPosition(currentEditedPostPosition)
        }
    }

    private fun updateUserFriends(friends: List<Friend>) {
        friendsCountTextView.text = if (friends.isNotEmpty()) "${friends.size}" else ""
        if (friends.size >= 6) {
            friendsAdapter = FriendsAdapter(friends.subList(0, 5), profileActivityPostsHandler)
            friendsRecyclerView.adapter = friendsAdapter
        } else {
            friendsAdapter = FriendsAdapter(friends, profileActivityPostsHandler)
            friendsRecyclerView.adapter = friendsAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_COVER_IMAGE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            profileActivityPostsHandler.uploadCoverImageToCloudStorage(bitmap)
        }
        if (requestCode == REQUEST_CODE_PROFILE_IMAGE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            profileActivityPostsHandler.uploadProfileImageToCloudStorage(bitmap)
        }
    }

    override fun onFriendClicked(friendId: String) {
        if (friendId == currentUser.id){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userId", friendId)
            startActivity(intent)
        }
        else{
            val intent = Intent(this, OthersProfileActivity::class.java)
            intent.putExtra("userId", friendId)
            startActivity(intent)
        }
    }
}
