package com.example.facebook_clone.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.User
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.CommentDocument
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.react.ReactDocument
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.model.post.share.ShareDocument
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileCoverBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileImageBottomSheet
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "ProfileActivity"
private const val REQUEST_CODE_COVER_IMAGE = 123
private const val REQUEST_CODE_PROFILE_IMAGE = 456

class ProfileActivity() : AppCompatActivity(), PostListener {
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null
    private lateinit var currentUser: User
    private lateinit var picasso: Picasso
    private var profilePostsAdapter: ProfilePostsAdapter? = null

    private var reactClicked = false
    private var mySingleReact: React? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        picasso = Picasso.get()
        val userLiveDate = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        userLiveDate?.observe(this, { user ->
            user?.let {
                currentUser = user
                updateUserInfo(user)

                //nested to get current user
                val postsLiveData =
                    postViewModel.getPostsWithoutOptions(auth.currentUser?.uid.toString())
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


    private fun updateUserInfo(user: User) {
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
            currentUser.profileImageUrl.toString()
        )

        //4 attaching the adapter to recycler view
        profilePostsRecyclerView.adapter = profilePostsAdapter

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

    private fun uploadCoverImageToUserCollection(photoUrl: String) {
        profileActivityViewModel.uploadCoverImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toastMessage(this, "Image uploaded successfully")
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
    }

    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profileActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toastMessage(this, "Image uploaded successfully")
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
    }

    override fun onReactButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean
    ) {
        //reactClicked ==> has to be got from backend
        if (!reactClicked) {
            val react = React(
                reactorId = interactorId,
                reactorName = interactorName,
                reactorImageUrl = interactorImageUrl,
                react = 1
            )
            mySingleReact = react
            createReact(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "React added", Toast.LENGTH_SHORT).show()
                    reactClicked = true
                    //  mTextView.setTextColor(getResources().getColor(R.color.<name_of_color>));
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }

        } else {
            deleteReact(mySingleReact!!, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.toastMessage(this, "React deleted")
                    reactClicked = false
                } else {
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
        reacted: Boolean
    ) {
        //SHOW THE DIALOG WITH DIFFERENT REACTIONS
    }

    override fun onCommentButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    ) {
        //Open comment bottom sheet
        CommentsBottomSheet(
            postPublisherId,
            postId,
            interactorId,
            interactorName,
            interactorImageUrl
        ).apply {
            show(supportFragmentManager, tag)
        }
    }

    override fun onShareButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    ) {
        val share = Share(
            sharerId = interactorId,
            sharerName = interactorName,
            sharerImageUrl = interactorImageUrl,
        )

        createShare(share, postId, postPublisherId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "You shared this post", Toast.LENGTH_SHORT).show()
            } else {
                Utils.toastMessage(this, task.exception?.message.toString())
            }
        }
    }

    override fun onReactLayoutClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String
    ) {
        //Open comment bottom sheet
        CommentsBottomSheet(
            postPublisherId,
            postId,
            interactorId,
            interactorName,
            interactorImageUrl
        ).apply {
            show(supportFragmentManager, tag)
        }
    }

    private fun createReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.createReact(react, postId, postPublisherId)

    }

    private fun deleteReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.deleteReact(react, postId, postPublisherId)
    }

    private fun createShare(share: Share, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.createShare(share, postId, postPublisherId)
    }

    private fun updatePostUI(postPublisherId: String, postId: String) {
        postViewModel.getPostById(postPublisherId, postId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.reference?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Utils.toastMessage(this, error.message.toString())
                        return@addSnapshotListener
                    }

                    val commentsResult = snapshot?.toObject(CommentDocument::class.java)?.comments
                    val reactsResult = snapshot?.toObject(ReactDocument::class.java)?.reacts
                    val sharesResult = snapshot?.toObject(ShareDocument::class.java)?.shares

                    val commentsList = commentsResult.orEmpty()
                    val reactsList = reactsResult.orEmpty()
                    val sharesList = sharesResult.orEmpty()

                    Log.i(TAG, "HHHH updatePostUI: $commentsList")
                    Log.i(TAG, "HHHH updatePostUI: $reactsList")
                    Log.i(TAG, "HHHH updatePostUI: $sharesList")

                }
            } else {
                Utils.toastMessage(this, task.exception?.message.toString())
            }
        }

    }


}