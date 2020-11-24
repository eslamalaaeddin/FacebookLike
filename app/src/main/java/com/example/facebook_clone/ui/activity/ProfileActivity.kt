package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.FriendsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.PostConfigurationsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileCoverBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileImageBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bioTextView
import kotlinx.android.synthetic.main.activity_profile.coverImageView
import kotlinx.android.synthetic.main.activity_profile.friendsRecyclerView
import kotlinx.android.synthetic.main.activity_profile.joinDateTextView
import kotlinx.android.synthetic.main.activity_profile.profileImageView
import kotlinx.android.synthetic.main.activity_profile.profilePostsRecyclerView
import kotlinx.android.synthetic.main.activity_profile.smallProfileImageView
import kotlinx.android.synthetic.main.activity_profile.userNameTextView
import kotlinx.android.synthetic.main.activity_profile.whatIsInYourMindTextView
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "ProfileActivity"
private const val REQUEST_CODE_COVER_IMAGE = 123
private const val REQUEST_CODE_PROFILE_IMAGE = 456

class ProfileActivity() : AppCompatActivity(), PostListener, FriendClickListener {
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()

    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null
    private lateinit var currentUser: User
    private lateinit var picasso: Picasso
    private var profilePostsAdapter: ProfilePostsAdapter? = null
    private lateinit var friendsAdapter: FriendsAdapter
    private var currentEditedPostPosition: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

//        val email = intent.getStringExtra("email").toString()
//        val password = intent.getStringExtra("password").toString()



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

        whatIsInYourMindTextView.setOnClickListener {
            val postCreatorDialog = PostCreatorDialog()
            postCreatorDialog.show(supportFragmentManager, "signature")
            postCreatorDialog
                .setUserNameAndProfileImageUrl(
                    currentUser.name.toString(),
                    currentUser.profileImageUrl.toString()
                )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserIdentity(user: User) {
        if (user.coverImageUrl != null) {
            picasso.load(user.coverImageUrl).into(coverImageView)
        }
        if (user.profileImageUrl != null) {
            picasso.load(user.profileImageUrl).into(profileImageView)
            picasso.load(user.profileImageUrl).into(smallProfileImageView)
        }
        if (user.biography != null) {
            bioTextView.text = user.biography
        }
        val date = android.text.format.DateFormat.format("MMM, yyyy", user.creationTime.toDate())

        userNameTextView.text = user.name
        joinDateTextView.text = "Joined $date"

    }

    private fun updateUserPosts(posts: List<Post>) {
        profilePostsAdapter = ProfilePostsAdapter(
            auth,
            posts,
            this,
            currentUser.name.toString(),
            currentUser.profileImageUrl.toString(),
            true
        )
        profilePostsRecyclerView.adapter = profilePostsAdapter
        if (currentEditedPostPosition != -1) {
            profilePostsRecyclerView.scrollToPosition(currentEditedPostPosition)
        }
    }

    private fun updateUserFriends(friends: List<Friend>) {
        friendsCountTextView.text = if (friends.isNotEmpty()) "${friends.size}" else ""
        if (friends.size >= 6) {
            friendsAdapter = FriendsAdapter(friends.subList(0, 5), this)
            friendsRecyclerView.adapter = friendsAdapter
        } else {
            friendsAdapter = FriendsAdapter(friends, this)
            friendsRecyclerView.adapter = friendsAdapter
        }

    }

    private fun uploadCoverImageToUserCollection(photoUrl: String) {
        profileActivityViewModel
            .uploadCoverImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                Utils.doAfterFinishing(
                    this,
                    task,
                    "Image uploaded successfully"
                )
            }
    }

    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profileActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                Utils.doAfterFinishing(this, task, "Image uploaded successfully")

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Cover image
        if (requestCode == REQUEST_CODE_COVER_IMAGE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            progressDialog = Utils.showProgressDialog(this, "Please wait...")
            profileActivityViewModel.uploadCoverImageToCloudStorage(bitmap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                            progressDialog?.dismiss()
                            uploadCoverImageToUserCollection(photoUrl.toString())
                        }
                    }
                }
        }
        //Profile image
        if (requestCode == REQUEST_CODE_PROFILE_IMAGE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            progressDialog = Utils.showProgressDialog(this, "Please wait...")
            profileActivityViewModel.uploadProfileImageToCloudStorage(bitmap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                            progressDialog?.dismiss()
                            uploadProfileImageToUserCollection(photoUrl.toString())
                        }
                    }
                }
        }
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


    override fun onReactButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
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


    override fun onReactButtonLongClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
        showReactsChooserDialog(
            interactorId,
            interactorName,
            interactorImageUrl,
            postId,
            postPublisherId,
            currentReact
        )
    }

    override fun onCommentButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
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

    override fun onShareButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
        val share = Share(
            sharerId = interactorId,
            sharerName = interactorName,
            sharerImageUrl = interactorImageUrl,
        )
        addShareToPost(share, postId, postPublisherId).addOnCompleteListener { task ->
            Utils.doAfterFinishing(this, task, "You shared this post")
        }
    }

    override fun onReactLayoutClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
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

    override fun onMediaPostClicked(mediaUrl: String) {
        //Image
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show(supportFragmentManager, "signature")
            imageViewerDialog.setMediaUrl(mediaUrl)
        }
        //video
        else {
            val videoIntent = Intent(this, VideoPlayerActivity::class.java)
            videoIntent.putExtra("videoUrl", mediaUrl)
            startActivity(videoIntent)
        }

    }

    override fun onPostMoreDotsClicked(post: Post) {
        val postConfigurationsBottomSheet = PostConfigurationsBottomSheet(post)
        postConfigurationsBottomSheet.show(
            supportFragmentManager,
            postConfigurationsBottomSheet.tag
        )
    }

    override fun onFriendClicked(friendId: String) {
        val intent = Intent(this, OthersProfileActivity::class.java)
        intent.putExtra("userId", friendId)
        startActivity(intent)
    }

}


