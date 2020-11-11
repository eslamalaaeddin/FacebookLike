package com.example.facebook_clone.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.Callback
import com.example.facebook_clone.helper.PostListener
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.User
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileCoverBottomSheet
import com.example.facebook_clone.ui.bottomsheet.ProfileImageBottomSheet
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
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
    private val postsViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null
    private lateinit var currentUser: User
    private lateinit var picasso: Picasso
    private var profilePostsAdapter: ProfilePostsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        picasso = Picasso.get()
//        profilePostsRecyclerView.isNestedScrollingEnabled = false;
        val userLiveDate = profileActivityViewModel.getUser(auth.currentUser?.uid.toString())
        userLiveDate?.observe(this, { user ->
            user?.let {
                currentUser = user
                updateUserInfo(user)
                updateUserPosts(user.id.toString())
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
                .setUserNameAndProfileImageUrl(currentUser.name.toString(), currentUser.profileImageUrl.toString())
        }

    }

    override fun onStop() {
        super.onStop()
        if (profilePostsAdapter != null){
            profilePostsAdapter?.stopListening()
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

    private fun updateUserPosts(userId: String){
        val options = postsViewModel.getPostsByUserId(userId)

        //3 initializing the adapter
        profilePostsAdapter = ProfilePostsAdapter(auth,
            options,
            this,
            currentUser.name.toString(),
            currentUser.profileImageUrl.toString()
        )

        //4 attaching the adapter to recycler view
        profilePostsRecyclerView.adapter = profilePostsAdapter

        //5 listening to the adapter
        profilePostsAdapter?.startListening()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Cover image
        if (requestCode == REQUEST_CODE_COVER_IMAGE) {
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
        if (requestCode == REQUEST_CODE_PROFILE_IMAGE) {
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

    override fun onCommentClicked(postPublisherId:String,postId: String,commenterId: String, commenterName: String, imageUrl: String) {
        //Open comment bottom sheet
        CommentsBottomSheet(postPublisherId,postId,commenterId, commenterName, imageUrl).apply {
            show(supportFragmentManager, tag)
        }
    }



}