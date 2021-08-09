package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.FriendsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.helper.posthandler.OthersProfileActivityPostsHandler
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.ui.bottomsheet.*
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.fragment.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.activity.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.activity.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_others_profile.*
import kotlinx.android.synthetic.main.activity_others_profile.friendsCountTextView
import kotlinx.android.synthetic.main.activity_others_profile.friendsRecyclerView
import kotlinx.android.synthetic.main.activity_others_profile.profilePostsRecyclerView
import kotlinx.android.synthetic.main.activity_others_profile.seeAllFriendsButton
import kotlinx.android.synthetic.main.activity_profile.bioTextView
import kotlinx.android.synthetic.main.activity_profile.coverImageView
import kotlinx.android.synthetic.main.activity_profile.joinDateTextView
import kotlinx.android.synthetic.main.activity_profile.profileImageView
import kotlinx.android.synthetic.main.activity_profile.smallUserImageView
import kotlinx.android.synthetic.main.activity_profile.userNameTextView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "OthersProfileActivity"

class OthersProfileActivity : AppCompatActivity(), PostListener, CommentsBottomSheetListener,
    FriendClickListener {
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private val postViewModel by viewModel<PostViewModel>()

    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
    private var crrentUser: User? = null
    private var currentNotificationId: String? = null
    private lateinit var userIAmViewing: User
    private var currentFriendRequest: FriendRequest? = null
    private var userIdIAmViewing: String? = null
    private lateinit var picasso: Picasso
    private var iAmFriend: Boolean = false
    private var currentEditedPostPosition: Int = -1
    private var profilePostsAdapter: ProfilePostsAdapter? = null
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var notificationsHandler: NotificationsHandler
    private lateinit var othersProfileActivityPostsHandler: OthersProfileActivityPostsHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others_profile)


        picasso = Picasso.get()
        notificationsHandler = NotificationsHandler(
            othersProfileActivityViewModel = othersProfileActivityViewModel,
            notificationsFragmentViewModel = notificationsFragmentViewModel
        )

        seeAllFriendsButton.setOnClickListener {
            showUserFriendsBottomSheet()
        }

        val myLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        myLiveData?.observe(this, { user ->
            user?.let {
                currentUser = user
                crrentUser = user
                notificationsHandler.notifierId = currentUser.id
                notificationsHandler.notifierName = currentUser.name
                notificationsHandler.notifierImageUrl = currentUser.profileImageUrl

                //Check for friend requests
                if (!currentUser.friendRequests.isNullOrEmpty()) {
//                     Toast.makeText(this, "a", Toast.LENGTH_SHORT).show()
                    currentUser.friendRequests?.forEach { friendRequest ->
                        //I changed my mind and want to delete the friend request that i sent.
                        if (friendRequest.fromId == currentUser.id) {
//                               Toast.makeText(this, "b", Toast.LENGTH_SHORT).show()
                            addFriendButton.visibility = View.INVISIBLE
                            addFriendButton.isEnabled = false
                            cancelRequestButton.isEnabled = true
                            cancelRequestButton.visibility = View.VISIBLE
                            currentFriendRequest = friendRequest
                        }
                        //Some one sent me a friend request and i entered his profile
                        else {
//                              Toast.makeText(this, "c", Toast.LENGTH_SHORT).show()
                            addFriendButton.isEnabled = false
                            cancelRequestButton.isEnabled = true
                            addFriendButton.visibility = View.INVISIBLE
                            cancelRequestButton.visibility = View.VISIBLE
                            currentFriendRequest = friendRequest
                        }
                    }
                }

                //there might be a friendship
                else if (currentUser.friends != null) {
//                     Toast.makeText(this, "d", Toast.LENGTH_SHORT).show()
                    if (currentUser.friends!!.isNotEmpty()) {
//                          Toast.makeText(this, "e", Toast.LENGTH_SHORT).show()
                        currentUser.friends?.forEach { friend ->
                            if (friend.id == userIdIAmViewing) {
//                                   Toast.makeText(this, "f", Toast.LENGTH_SHORT).show()
                                addFriendButton.isEnabled = false
                                cancelRequestButton.isEnabled = false
                                addFriendButton.visibility = View.INVISIBLE
                                cancelRequestButton.visibility = View.INVISIBLE
                                messageButton.isEnabled = true
                                messageButton.visibility = View.VISIBLE
                            } else {
//                                Toast.makeText(this, "g", Toast.LENGTH_SHORT).show()
                                cancelRequestButton.isEnabled = false
                                cancelRequestButton.visibility = View.INVISIBLE
                                addFriendButton.isEnabled = true
                                addFriendButton.visibility = View.VISIBLE
                            }
                        }

                    } else {
                        addFriendButton.isEnabled = true
                        addFriendButton.visibility = View.VISIBLE
                        cancelRequestButton.isEnabled = false
                        cancelRequestButton.visibility = View.INVISIBLE
                    }

                } else {
//                     Toast.makeText(this, "h", Toast.LENGTH_SHORT).show()
                    addFriendButton.isEnabled = true
                    cancelRequestButton.isEnabled = false
                    addFriendButton.visibility = View.VISIBLE
                    cancelRequestButton.visibility = View.INVISIBLE
                }
            }
        })

        val friendRequesterId = intent.getStringExtra("friendRequester")
        if (userIdIAmViewing == null && friendRequesterId != null) {
            userIdIAmViewing = friendRequesterId
            notificationsHandler.notifiedId = userIdIAmViewing
        } else {
            userIdIAmViewing = intent.getStringExtra("userId").toString()
        }

        notificationsHandler.notifiedId = userIdIAmViewing
        notificationsHandler.postPublisherId = userIdIAmViewing
        val userLiveDate = profileActivityViewModel.getAnotherUser(userIdIAmViewing!!)
        userLiveDate?.observe(this, { user ->
            user?.let {
                userIAmViewing = user
                // notificationsHandler.notifiedToken = userIAmViewing.token
                notificationsHandler.notifiedToken = user.token
                othersProfileActivityPostsHandler = OthersProfileActivityPostsHandler(
                    this,
                    postViewModel,
                    notificationsFragmentViewModel,
                    othersProfileActivityViewModel
                )
                updateUserInfo(user)
                updateUserFriends(user.friends.orEmpty())
                iAmFriend()
//                if (!user.friends.isNullOrEmpty()) {
//                    friendsAdapter = FriendsAdapter(user.friends!!, this)
//                    friendsRecyclerView.adapter = friendsAdapter
//                    iAmFriend()
//                }
                val postsLiveData =
                    postViewModel.getUserProfilePostsLiveData(userIdIAmViewing!!)
                postsLiveData.observe(this, { posts ->
                    updateUserPosts(posts)
                })

                val notificationsLiveData =
                    notificationsFragmentViewModel.getNotificationsLiveData(userIdIAmViewing!!)
                notificationsLiveData.observe(this, { notifications ->

                    if (notifications != null) {
                        val userNotificationsIds = user.notificationsIds
                        if (userNotificationsIds != null) {
                            notifications.forEach { notification ->
                                if (userNotificationsIds?.contains(notification.id.toString())!!) {
                                    //CURRENT NOT ID
                                    currentNotificationId = notification.id.toString()
                                }
                            }
                        }
                    }
                })
            }
        })

        profileImageView.setOnClickListener {
            showUserImage(userIAmViewing.profileImageUrl.toString())
        }

        coverImageView.setOnClickListener {
            showUserImage(userIAmViewing.coverImageUrl.toString())
        }

        addFriendButton.setOnClickListener { sendUserFriendRequest() }

        cancelRequestButton.setOnClickListener {
                                deleteFriendRequestFromMeAndHim()
                                //deleteFriendRequestNotificationFromMyNotifications()
                                if (currentNotificationId != null) {
                                    deleteNotification(userIdIAmViewing.orEmpty(), currentNotificationId.orEmpty())
                                }
        }

        messageButton.setOnClickListener {
            Toast.makeText(this, "ههه؟", Toast.LENGTH_SHORT).show()
        }

        userRelationsButton.setOnClickListener {
            val usersRelationsBottomSheet = UserRelationsBottomSheet(userIdIAmViewing.toString())
            usersRelationsBottomSheet.show(supportFragmentManager, usersRelationsBottomSheet.tag)
        }
    }

    private fun deleteFriendRequestNotificationFromMyNotifications(){
        val notificationsLiveData =
            notificationsFragmentViewModel.getNotificationsLiveData(currentUser.id.toString())
        notificationsLiveData.observe(this, { notifications ->

            if (notifications != null) {
                val userNotificationsIds = crrentUser?.notificationsIds
                if (userNotificationsIds != null) {
                    notifications.forEach { notification ->
                        if (userNotificationsIds?.contains(notification.id.toString())!!) {
                            //CURRENT NOT ID
                            currentNotificationId = notification.id.toString()
                        }
                    }
                }
            }
        })
    }

    private fun deleteFriendRequestFromMeAndHim() {
        Toast.makeText(this, "REQ", Toast.LENGTH_SHORT).show()
        if (currentFriendRequest != null) {
            Toast.makeText(this, "NOT NULL", Toast.LENGTH_SHORT).show()

            othersProfileActivityViewModel.removeFriendRequestFromHisDocument(currentFriendRequest!!)
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        //Update ui
                        othersProfileActivityViewModel.removeFriendRequestFromMyDocument(
                            currentFriendRequest!!
                        ).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
//                                deleteNotification(auth.currentUser?.uid.toString(), notificationId)
                                Log.i(TAG, "MMMM FromMeAndHim:$userIdIAmViewing")
                                //Log.i(TAG, "MMMM FromMeAndHim:$notificationId")
                                //deleteNotification(userIdIAmViewing.orEmpty(), notificationId)
                            } else {
                                Toast.makeText(
                                    this,
                                    task2.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            task1.exception?.message,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

    private fun deleteNotification(notifiedId: String, notificationId: String) {
        notificationsFragmentViewModel.deleteNotificationById(notifiedId, notificationId)
    }

    private fun showUserFriendsBottomSheet() {
        val userFriendsBottomSheet =
            UserFriendsBottomSheet(friends = userIAmViewing.friends.orEmpty(), this)
        userFriendsBottomSheet.show(supportFragmentManager, userFriendsBottomSheet.tag)
    }

    private fun showUserImage(imageUrl: String) {
        val imageViewerDialog = ImageViewerDialog()
        imageViewerDialog.show(supportFragmentManager, "signature")
        imageViewerDialog.setMediaUrl(imageUrl)
    }

    private fun updateUserPosts(posts: List<Post>) {
        profilePostsAdapter = ProfilePostsAdapter(
            auth,
            posts,
            this,
            currentUser.name.toString(),
            currentUser.profileImageUrl.toString(),
            iAmFriend,
            userIdIAmViewing.toString()
        )
        profilePostsRecyclerView.adapter = profilePostsAdapter
        if (currentEditedPostPosition != -1) {
            profilePostsRecyclerView.scrollToPosition(currentEditedPostPosition)
        }
    }

    private fun updateUserInfo(user: User) {
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

    private fun sendUserFriendRequest() {
        val friendRequest = FriendRequest(fromId = currentUser.id, toId = userIdIAmViewing)
        othersProfileActivityViewModel.addFriendRequestToHisDocument(friendRequest)
            .addOnCompleteListener { task1 ->
                if (task1.isSuccessful) {
                    othersProfileActivityViewModel.addFriendRequestToMyDocument(friendRequest)
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                notificationsHandler.also {
                                    it.notificationType = "friendRequest"
                                    it.handleNotificationCreationAndFiring()
                                }
                                //Toast.makeText(this, "Notifiacation", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, task2.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
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
        val userToBeNotified = profileActivityViewModel.getAnotherUser(post.publisherId.orEmpty())
        userToBeNotified?.observe(this) { user ->
            val token = user.token
            othersProfileActivityPostsHandler.onReactButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                postPosition,
                token
            )
            userToBeNotified.removeObservers(this)
        }
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
        val userToBeNotified = profileActivityViewModel.getAnotherUser(post.publisherId.orEmpty())
        userToBeNotified?.observe(this) { user ->
            val token = user.token
            othersProfileActivityPostsHandler.onReactButtonLongClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                postPosition,
                token
            )
            userToBeNotified.removeObservers(this)
        }
    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        othersProfileActivityPostsHandler.onCommentButtonClicked(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            postPosition
        )
    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String?
    ) {
        val userToBeNotified = profileActivityViewModel.getAnotherUser(post.publisherId.orEmpty())
        userToBeNotified?.observe(this) { user ->
            val token = user.token
            othersProfileActivityPostsHandler.onShareButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                postPosition,
                token
            )
            userToBeNotified.removeObservers(this)
        }
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        othersProfileActivityPostsHandler.onCommentButtonClicked(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            postPosition
        )
    }

    override fun onMediaPostClicked(mediaUrl: String) {
        othersProfileActivityPostsHandler.onMediaPostClicked(mediaUrl)
    }

    override fun onPostMoreDotsClicked(interactorId: String, post: Post, shared: Boolean?) {

    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {

    }

    override fun onAnotherUserCommented(
        notifierId: String,
        notifierName: String,
        notifierImageUrl: String,
        notifiedId: String,
        notifiedToken: String,
        notificationType: String,
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentId: String
    ) {}

    override fun onFriendClicked(friendId: String) {
        if (friendId == currentUser.id) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = Intent(this, OthersProfileActivity::class.java)
            intent.putExtra("userId", friendId)
            startActivity(intent)
        }
    }

    private fun iAmFriend() {
        userIAmViewing.friends?.forEach { friend ->
            if (friend.id == currentUser.id) {
                iAmFriend = true
            }
        }
    }

    private fun updateUserFriends(friends: List<Friend>) {
        friendsCountTextView.text = if (friends.isNotEmpty()) "${friends.size} friends" else ""
        if (friends.size >= 6) {
            friendsAdapter = FriendsAdapter(friends.subList(0, 5), this)
            friendsRecyclerView.adapter = friendsAdapter
        } else {
            friendsAdapter = FriendsAdapter(friends, this)
            friendsRecyclerView.adapter = friendsAdapter
        }

    }

}