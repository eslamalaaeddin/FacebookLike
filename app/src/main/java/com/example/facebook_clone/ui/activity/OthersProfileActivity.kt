package com.example.facebook_clone.ui.activity

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.FriendsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.BaseApplication
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.notification.Notifier
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_others_profile.*
import kotlinx.android.synthetic.main.activity_others_profile.friendsRecyclerView
import kotlinx.android.synthetic.main.activity_others_profile.profilePostsRecyclerView
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bioTextView
import kotlinx.android.synthetic.main.activity_profile.coverImageView
import kotlinx.android.synthetic.main.activity_profile.joinDateTextView
import kotlinx.android.synthetic.main.activity_profile.profileImageView
import kotlinx.android.synthetic.main.activity_profile.smallProfileImageView
import kotlinx.android.synthetic.main.activity_profile.userNameTextView
import kotlinx.android.synthetic.main.activity_testing.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "OthersProfileActivity"
class OthersProfileActivity : AppCompatActivity(), PostListener {
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
    private lateinit var userIAmViewing: User
    private var currentFriendRequest: FriendRequest? = null
    private lateinit var userIdIAmViewing: String
    private lateinit var picasso: Picasso
    private var currentEditedPostPosition: Int = -1
    private var profilePostsAdapter: ProfilePostsAdapter? = null
    private lateinit var friendsAdapter: FriendsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others_profile)


        picasso = Picasso.get()

        val myLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        myLiveData?.observe(this, { user ->
            user?.let {
                currentUser = user
                Log.i(TAG, "FAWZY onCreate: $currentUser")
                //Check for friend requests
                if (!currentUser.friendRequests.isNullOrEmpty()) {
                    currentUser.friendRequests?.forEach { friendRequest ->
                        if (friendRequest.fromId == currentUser.id) {
                            addFriendButton.visibility = View.INVISIBLE
                            addFriendButton.isEnabled = false
                            cancelRequestButton.isEnabled = true
                            cancelRequestButton.visibility = View.VISIBLE
                            currentFriendRequest = friendRequest
                        } else {
                            addFriendButton.isEnabled = true
                            cancelRequestButton.isEnabled = false
                            addFriendButton.visibility = View.VISIBLE
                            cancelRequestButton.visibility = View.INVISIBLE
                        }
                    }
                }

//                if there is a friendship
                else if (currentUser.friends != null) {
                currentUser.friends?.forEach { friend ->
                    if (friend.id == userIdIAmViewing) {
                        addFriendButton.isEnabled = false
                        cancelRequestButton.isEnabled = false
                        addFriendButton.visibility = View.INVISIBLE
                        cancelRequestButton.visibility = View.INVISIBLE
                    }
                }
            } else {
                addFriendButton.isEnabled = true
                cancelRequestButton.isEnabled = false
                addFriendButton.visibility = View.VISIBLE
                cancelRequestButton.visibility = View.INVISIBLE
            }
            }
        })

        userIdIAmViewing = intent.getStringExtra("userId").toString()
        val userLiveDate = profileActivityViewModel.getAnotherUser(userIdIAmViewing)
        userLiveDate?.observe(this, { user ->
            user?.let {
                userIAmViewing = user
                updateUserInfo(user)
                if (!user.friends.isNullOrEmpty()){
                    friendsAdapter = FriendsAdapter(user.friends!!)
                    friendsRecyclerView.adapter = friendsAdapter
                }
                val postsLiveData =
                    postViewModel.getPostsWithoutOptions(userIdIAmViewing)
                postsLiveData.observe(this, { posts ->
                    updateUserPosts(posts)
                })
            }
        })





        addFriendButton.setOnClickListener { sendUserFriendRequest() }

        cancelRequestButton.setOnClickListener {
            othersProfileActivityViewModel.removeFriendRequestFromHisDocument(currentFriendRequest!!).addOnCompleteListener { task1 ->
                if (task1.isSuccessful){
                    //Update ui
                    othersProfileActivityViewModel.removeFriendRequestFromMyDocument(currentFriendRequest!!).addOnCompleteListener{task2 ->
                        if (task2.isSuccessful){
                            Toast.makeText(this, "Friend request is removed successfully", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this, task2.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun sendUserFriendRequest(){
        val friendRequest = FriendRequest(
            fromId = currentUser.id,
            toId = userIdIAmViewing,
        )

        othersProfileActivityViewModel.addFriendRequestToHisDocument(friendRequest).addOnCompleteListener { task1 ->
            if (task1.isSuccessful){
                //Update ui
                othersProfileActivityViewModel.addFriendRequestToMyDocument(friendRequest).addOnCompleteListener{task2 ->
                    if (task2.isSuccessful){
                        val notifier = Notifier(
                            id = currentUser.id,
                            imageUrl = currentUser.profileImageUrl,
                            name = currentUser.name
                        )
                        val notification = Notification("friendRequest", notifier)

                        othersProfileActivityViewModel
                            .addNotificationToNotificationsCollection(notification,userIdIAmViewing)
                            .addOnCompleteListener { task3 ->
                            if (task3.isSuccessful){
                                Toast.makeText(this, "Friend request sent successfully", Toast.LENGTH_SHORT).show()
                                //fireFriendRequestNotification(notification)
                            }else{
                                Toast.makeText(this, task3.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(this, task2.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fireFriendRequestNotification(notification: Notification){
        var bitmap : Bitmap? = null
        Glide.with(this)
            .asBitmap()
            .load(currentUser.profileImageUrl)
            .circleCrop()
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmap = resource
                    notification.notifier?.imageBitmap = bitmap
                }
            })

//        button.setOnClickListener {
//            BaseApplication.fireNotification(notification!!, MainActivity::class.java)
//        }
    }

    private fun updateUserPosts(posts: List<Post>) {
        //this check is to prevent recycler view from auto scrolling
        //so, the ui have to be updated first from client side in addition to updating it from the server side

//        if (profilePostsAdapter == null) {
        profilePostsAdapter = ProfilePostsAdapter(
            auth,
            posts,
            this,
            currentUser.name.toString(),
            currentUser.profileImageUrl.toString()
        )
        profilePostsRecyclerView.adapter = profilePostsAdapter
        //position has to be changed post
        if (currentEditedPostPosition != -1){
            profilePostsRecyclerView.scrollToPosition(currentEditedPostPosition)
        }
//        }else{
//            profilePostsAdapter?.notifyDataSetChanged()
//        }
    }

    override fun onReactButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postReacts: List<React>?,
        postPosition: Int
    ) {
    }

    override fun onReactButtonLongClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postReacts: List<React>?,
        postPosition: Int
    ) {

    }

    override fun onCommentButtonClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
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
        interactorImageUrl: String,
        postPosition: Int
    ) {
    }

    override fun onReactLayoutClicked(
        postPublisherId: String,
        postId: String,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {

    }

}