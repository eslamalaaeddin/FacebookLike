package com.example.facebook_clone.ui.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.FriendsAdapter
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.ui.bottomsheet.CommentsBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_others_profile.*
import kotlinx.android.synthetic.main.activity_others_profile.friendsRecyclerView
import kotlinx.android.synthetic.main.activity_others_profile.profilePostsRecyclerView
import kotlinx.android.synthetic.main.activity_profile.bioTextView
import kotlinx.android.synthetic.main.activity_profile.coverImageView
import kotlinx.android.synthetic.main.activity_profile.joinDateTextView
import kotlinx.android.synthetic.main.activity_profile.profileImageView
import kotlinx.android.synthetic.main.activity_profile.smallProfileImageView
import kotlinx.android.synthetic.main.activity_profile.userNameTextView
import kotlinx.android.synthetic.main.long_clicked_reacts_button.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "OthersProfileActivity"
class OthersProfileActivity : AppCompatActivity(), PostListener, CommentsBottomSheetListener, FriendClickListener {
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
    private var currentNotificationId: String? = null
    private lateinit var userIAmViewing: User
    private var currentFriendRequest: FriendRequest? = null
    private lateinit var userIdIAmViewing: String
    private lateinit var picasso: Picasso
    private var iAmFriend: Boolean = false
    private var currentEditedPostPosition: Int = -1
    private var profilePostsAdapter: ProfilePostsAdapter? = null
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var notificationsHandler: NotificationsHandler
    private var choice: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others_profile)

        picasso = Picasso.get()
        notificationsHandler = NotificationsHandler(
            othersProfileActivityViewModel = othersProfileActivityViewModel,
            notificationsFragmentViewModel = notificationsFragmentViewModel
        )

        val myLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        myLiveData?.observe(this, { user ->
            user?.let {
                currentUser = user

                notificationsHandler.notifierId = currentUser.id
                notificationsHandler.notifierName = currentUser.name
                notificationsHandler.notifierImageUrl = currentUser.profileImageUrl

                //Check for friend requests
                if (!currentUser.friendRequests.isNullOrEmpty()) {
                 //   Toast.makeText(this, "a", Toast.LENGTH_SHORT).show()
                    currentUser.friendRequests?.forEach { friendRequest ->
                        if (friendRequest.fromId == currentUser.id) {
                          //  Toast.makeText(this, "b", Toast.LENGTH_SHORT).show()
                            addFriendButton.visibility = View.INVISIBLE
                            addFriendButton.isEnabled = false
                            cancelRequestButton.isEnabled = true
                            cancelRequestButton.visibility = View.VISIBLE
                            currentFriendRequest = friendRequest
                        } else {
                           // Toast.makeText(this, "c", Toast.LENGTH_SHORT).show()
                            addFriendButton.isEnabled = true
                            cancelRequestButton.isEnabled = false
                            addFriendButton.visibility = View.VISIBLE
                            cancelRequestButton.visibility = View.INVISIBLE
                        }
                    }
                }

                //there might be a friendship
                else if (currentUser.friends != null) {
                   // Toast.makeText(this, "d", Toast.LENGTH_SHORT).show()
                    if (currentUser.friends!!.isNotEmpty()) {
                      //  Toast.makeText(this, "e", Toast.LENGTH_SHORT).show()
                        currentUser.friends?.forEach { friend ->
                            if (friend.id == userIdIAmViewing) {
                             //   Toast.makeText(this, "f", Toast.LENGTH_SHORT).show()
                                addFriendButton.isEnabled = false
                                cancelRequestButton.isEnabled = false
                                addFriendButton.visibility = View.INVISIBLE
                                cancelRequestButton.visibility = View.INVISIBLE
                                messageButton.isEnabled = true
                                messageButton.visibility = View.VISIBLE
                            }
                            else{
                                cancelRequestButton.isEnabled = false
                                cancelRequestButton.visibility = View.INVISIBLE
                            }
                        }

                    }else{
                        addFriendButton.isEnabled = true
                        addFriendButton.visibility = View.VISIBLE
                        cancelRequestButton.isEnabled = false
                        cancelRequestButton.visibility = View.INVISIBLE
                    }

                }
                else {
                   // Toast.makeText(this, "g", Toast.LENGTH_SHORT).show()
                    addFriendButton.isEnabled = true
                    cancelRequestButton.isEnabled = false
                    addFriendButton.visibility = View.VISIBLE
                    cancelRequestButton.visibility = View.INVISIBLE
                }
            }
        })


        userIdIAmViewing = intent.getStringExtra("userId").toString()
        notificationsHandler.notifiedId = userIdIAmViewing
        val userLiveDate = profileActivityViewModel.getAnotherUser(userIdIAmViewing)
        userLiveDate?.observe(this, { user ->
            user?.let {
                userIAmViewing = user
                notificationsHandler.notifiedToken = userIAmViewing.token
                updateUserInfo(user)
                if (!user.friends.isNullOrEmpty()) {
                    friendsAdapter = FriendsAdapter(user.friends!!, this)
                    friendsRecyclerView.adapter = friendsAdapter
                    iAmFriend()
                }
                val postsLiveData =
                    postViewModel.getPostsWithoutOptions(userIdIAmViewing)
                postsLiveData.observe(this, { posts ->
                    updateUserPosts(posts)
                })

                val notificationsLiveData =
                    notificationsFragmentViewModel.getNotificationsLiveData(userIdIAmViewing)
                notificationsLiveData.observe(this, { notifications ->
                    if (notifications != null) {
                        val userNotificationsIds = user.notificationsIds
                        if (userNotificationsIds != null){
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
//
//        addFriendButton.setOnClickListener { sendUserFriendRequest() }
//
//        cancelRequestButton.setOnClickListener {
//            othersProfileActivityViewModel.removeFriendRequestFromHisDocument(currentFriendRequest!!).addOnCompleteListener { task1 ->
//                if (task1.isSuccessful){
//                    //Update ui
//                    othersProfileActivityViewModel.removeFriendRequestFromMyDocument(currentFriendRequest!!).addOnCompleteListener{task2 ->
//                        if (task2.isSuccessful){
//                            if (currentNotificationId != null){
//                                handleNotificationDeleting(currentNotificationId!!, userIdIAmViewing)
//                            }
//                        }else{
//                            Toast.makeText(this, task2.exception?.message, Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }else{
//                    Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
        
        messageButton.setOnClickListener {
            Toast.makeText(this, "- -\n__", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateUserPosts(posts: List<Post>) {
        profilePostsAdapter = ProfilePostsAdapter(
            auth,
            posts,
            this,
            currentUser.name.toString(),
            currentUser.profileImageUrl.toString(),
            iAmFriend
         )
        profilePostsRecyclerView.adapter = profilePostsAdapter
        if (currentEditedPostPosition != -1){
            profilePostsRecyclerView.scrollToPosition(currentEditedPostPosition)
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
//
//    private fun sendUserFriendRequest(){
//        val friendRequest = FriendRequest(
//            fromId = currentUser.id,
//            toId = userIdIAmViewing,
//        )
//        othersProfileActivityViewModel.addFriendRequestToHisDocument(friendRequest).addOnCompleteListener { task1 ->
//            if (task1.isSuccessful){
//                //Update ui
//                othersProfileActivityViewModel.addFriendRequestToMyDocument(friendRequest).addOnCompleteListener{task2 ->
//                    if (task2.isSuccessful){
//                        handleNotificationCreationAndFiringAndAdditionToDB(
//                            notificationType = "friendRequest",
//                            postId = null,
//                            commentPosition = null,
//                            commentId = null
//                        )
//                    }else{
//                        Toast.makeText(this, task2.exception?.message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }else{
//                Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private fun addNotificationIdToHisDocument(notificationId: String, hisId: String) {
//        othersProfileActivityViewModel.addNotificationIdToNotifiedDocument(notificationId, hisId)
//    }

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
        //I did not react
        if (!reacted){
            //UPDATE REACTED VALUE WITH 1
            postViewModel.updateReactedValue(postPublisherId, postId, 1)
            val myReact = createReact(interactorId, interactorName, interactorImageUrl,1)
            addReactToDb(myReact, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.reactType = 1
                        it.postId = postId
                        it.handleNotificationCreationAndFiring()
                    }
                }
                else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        //If you have reacted --> delete react
        else {
            //UPDATE REACTED VALUE WITH NULL
            //postViewModel.updateReactedValue(postPublisherId, postId, currentReact!!)
            deleteReact(currentReact!!, postId, postPublisherId).addOnCompleteListener { task ->
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
            showReactsChooserDialog(interactorId, interactorName, interactorImageUrl, postId, postPublisherId, currentReact)
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
            interactorImageUrl,
            this,
            userIAmViewing.token.toString()
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

        createShare(share, postId, postPublisherId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                notificationsHandler.also {
                    it.notificationType = "share"
                    it.postId = postId
                    it.handleNotificationCreationAndFiring()
                }
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
        interactorImageUrl: String,
        postPosition: Int
    ) {

    }

    override fun onMediaPostClicked(mediaUrl: String) {
        //Image
        if (mediaUrl.contains("jpeg")) {
            val imageViewerDialog = ImageViewerDialog()
            imageViewerDialog.show(supportFragmentManager, "signature")
            imageViewerDialog.setMediaUrl(mediaUrl)
        }
        //video(I chosed an activity to show media controllers)
        else{
            val videoIntent = Intent(this, VideoPlayerActivity::class.java)
            videoIntent.putExtra("videoUrl", mediaUrl)
            startActivity(videoIntent)
        }
    }

    override fun onPostMoreDotsClicked(post: Post) {

    }

    private fun addReactToDb(react: React, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.addReactToDB(react, postId, postPublisherId)

    }

    private fun createReact(interactorId: String, interactorName: String, interactorImageUrl: String,reactType: Int?): React{
        return React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl,
            react = reactType
        )
    }

    private fun deleteReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.deleteReact(react, postId, postPublisherId)
    }

    private fun createShare(share: Share, postId: String, postPublisherId: String): Task<Void> {
        return postViewModel.createShare(share, postId, postPublisherId)
    }

    override fun onAnotherUserCommented(commentPosition: Int, commentId: String, postId: String) {
        notificationsHandler.also {
            it.notificationType = "commentOnPost"
            it.postId = postId
            it.commentPosition = commentPosition
            it.handleNotificationCreationAndFiring()
        }
    }

    override fun onFriendClicked(friendId: String) {
        if (friendId == currentUser.id){
            startActivity(Intent(this, ProfileActivity::class.java))
        }else{
            val intent = Intent(this, OthersProfileActivity::class.java)
            intent.putExtra("userId", friendId)
            startActivity(intent)
        }

    }

    private fun iAmFriend(){
        userIAmViewing.friends?.forEach { friend ->
            if (friend.id == currentUser.id){
                iAmFriend = true
            }
        }
    }
//
    private fun showReactsChooserDialog(interactorId: String,
                                        interactorName: String,
                                        interactorImageUrl: String,
                                        postId: String,
                                        postPublisherId: String,
                                        currentReact: React?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.long_clicked_reacts_button)
        val react =  React(
            reactorId = interactorId,
            reactorName = interactorName,
            reactorImageUrl = interactorImageUrl
        )
        dialog.loveReactButton.setOnClickListener {
            react.react = 2
            if (currentReact != null){
                deleteReact(currentReact, postId, postPublisherId)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,2)
            addReactToDb(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.postId = postId
                        it.reactType = 2
                        it.handleNotificationCreationAndFiring()
                    }
                }else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.careReactButton.setOnClickListener {
            react.react = 3
            if (currentReact != null){
                deleteReact(currentReact, postId, postPublisherId)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,3)
            addReactToDb(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.postId = postId
                        it.reactType = 3
                        it.handleNotificationCreationAndFiring()
                    }
                }else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.hahaReactButton.setOnClickListener {
            react.react = 4
            if (currentReact != null){
                deleteReact(currentReact, postId, postPublisherId)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,4)
            addReactToDb(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.postId = postId
                        it.reactType = 4
                        it.handleNotificationCreationAndFiring()
                    }
                }else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.wowReactButton.setOnClickListener {
            react.react = 5
            if (currentReact != null){
                deleteReact(currentReact, postId, postPublisherId)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,5)
            addReactToDb(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.postId = postId
                        it.reactType = 5
                        it.handleNotificationCreationAndFiring()
                    }
                }else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.sadReactButton.setOnClickListener {
            react.react = 6
            if (currentReact != null){
                deleteReact(currentReact, postId, postPublisherId)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,6)
            addReactToDb(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.postId = postId
                        it.reactType = 6
                        it.handleNotificationCreationAndFiring()
                    }
                }else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.angryReactButton.setOnClickListener {
            react.react = 7
            if (currentReact != null){
                deleteReact(currentReact, postId, postPublisherId)
            }
            //postViewModel.updateReactedValue(postPublisherId, postId,7)
            addReactToDb(react, postId, postPublisherId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notificationsHandler.also {
                        it.notificationType = "reactOnPost"
                        it.postId = postId
                        it.reactType = 7
                        it.handleNotificationCreationAndFiring()
                    }
                }else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.show()

    }


}