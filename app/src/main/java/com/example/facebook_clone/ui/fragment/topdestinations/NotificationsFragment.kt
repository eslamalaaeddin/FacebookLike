package com.example.facebook_clone.ui.fragment.topdestinations

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.NotificationsAdapter
import com.example.facebook_clone.helper.listener.NotificationListener
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.followed.Followed
import com.example.facebook_clone.model.user.follower.Follower
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.model.user.friendrequest.FriendRequest
import com.example.facebook_clone.ui.activity.GroupActivity
import com.example.facebook_clone.ui.activity.OthersProfileActivity
import com.example.facebook_clone.ui.activity.PostViewerActivity
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "NotificationsFragment"

class NotificationsFragment : Fragment(R.layout.fragment_notifications), NotificationListener {
    private val notificationsFragmentViewModel
            by viewModel<NotificationsFragmentViewModel>()
    private val othersProfileActivityViewModel
            by viewModel<OthersProfileActivityViewModel>()
    private val profileActivityViewModel
            by viewModel<ProfileActivityViewModel>()
    private val groupViewModel
            by viewModel<GroupsViewModel>()

    private val auth: FirebaseAuth by inject()
    private val notifiedUserId = auth.currentUser?.uid.toString()//Current user id

    private lateinit var notificationsAdapter: NotificationsAdapter
    private var currentFriendRequest: FriendRequest? = null
    private lateinit var currentUser: User
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                notificationsRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        val currentUserLiveData = profileActivityViewModel.getMe(notifiedUserId)
        currentUserLiveData?.observe(viewLifecycleOwner, { user ->
            currentUser = user
            user.friendRequests?.forEach { friendRequest ->
                if (friendRequest.toId == notifiedUserId) {
                    currentFriendRequest = friendRequest
                }
            }
        })
        val notificationsLiveData =
            notificationsFragmentViewModel.getNotificationsLiveData(notifiedUserId)
        notificationsLiveData.observe(viewLifecycleOwner, { notifications ->
            notificationsAdapter = NotificationsAdapter(notifications, this)
            notificationsRecyclerView.adapter = notificationsAdapter
        })
    }

    override fun onClickFriendRequestNotification(userId: String) {
        val intent = Intent(requireContext(), OthersProfileActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    override fun onClickDeleteFriendRequestNotification(
        notifiedId: String,
        notificationId: String
    ) {
        deleteFriendRequestFromMeAndHim(notificationId)
        deleteNotification(auth.currentUser?.uid.toString(), notificationId)
    }

    override fun onClickConfirmFriendRequestNotification(
        notifiedId: String,
        notificationId: String,
        userId: String,
        userName: String,
        userImageUrl: String
    ) {
        val meAsFriend = Friend(userId, userName, userImageUrl)
        val himAsFriend = Friend(currentUser.id, currentUser.name, currentUser.profileImageUrl)
        createFriendshipBetweenMeAndHim(notificationId, meAsFriend, himAsFriend)

//        val meAsAFollower = Follower(userId, userName, userImageUrl)
//        val meAsAFollowing = Followed(userId, userName, userImageUrl)
//        //following == followed
//        val himAsAFollowing = Followed(currentUser.id, currentUser.name, currentUser.profileImageUrl)
//        val himAsAFollower = Follower(currentUser.id, currentUser.name, currentUser.profileImageUrl)
//
//        othersProfileActivityViewModel.addMeAsAFollowerToHisDocument(currentUser.id.toString(), meAsAFollower)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful){
//                    othersProfileActivityViewModel.addHimAsAFollowingToMyDocument(userId, himAsAFollowing)
//                }
//            }

        othersProfileActivityViewModel.deleteNotificationIdFromNotifiedDocument(
            notificationId,
            auth.currentUser?.uid.toString()
        )
    }

    override fun onClickReactOnPostNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    ) {
        navigateToPost(postPublisherId, postId, firstCollectionType, creatorReferenceId, secondCollectionType)
    }

    override fun onClickShareOnPostNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    ) {
        navigateToPost(postPublisherId, postId, firstCollectionType, creatorReferenceId, secondCollectionType)
    }

    override fun onClickCommentOnPostNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentPosition: Int
    ) {
        navigateToComment(postPublisherId, postId, commentPosition, firstCollectionType, creatorReferenceId, secondCollectionType)
    }

    override fun onClickReactsOnCommentNotification(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentPosition: Int
    ) {
        navigateToComment(postPublisherId, postId, commentPosition, firstCollectionType, creatorReferenceId, secondCollectionType)
    }

    override fun onNotificationLongClicked(notification: Notification) {
        showDeleteNotificationConfirmationDialog(notification)
    }

    override fun onClickOnGroupInvitationNotification(notifierId: String, groupId: String) {
        Toast.makeText(requireContext(), "$notifierId || $groupId", Toast.LENGTH_SHORT).show()
    }

    override fun onAcceptGroupInvitationButtonClicked(groupId: String, notificationId: String) {

        val member = Member(
            id = currentUser.id,
            name = currentUser.name,
            imageUrl = currentUser.profileImageUrl,
            blocked = false
        )
        groupViewModel.addMemberToGroup(member, groupId).addOnSuccessListener {
            val groupLiveData = groupViewModel.getGroupLiveData(groupId)
            groupLiveData.observe(viewLifecycleOwner) {
                val semiGroup = SemiGroup(it.id, it.name, it.coverImageUrl)
                addGroupToUserGroups(userId = currentUser.id.toString(), semiGroup)
            }

            deleteNotification(currentUser.id.toString(), notificationId)
        }
    }

    override fun onCancelGroupInvitationButtonClicked(notifiedId: String, notificationId: String) {
        deleteNotification(notifiedId, notificationId)
    }

    private fun addGroupToUserGroups(userId: String, semiGroup: SemiGroup) {
        groupViewModel.addGroupToUserGroups(userId, semiGroup)
    }

    private fun deleteFriendRequestFromMeAndHim(notificationId: String) {
        if (currentFriendRequest != null) {
            othersProfileActivityViewModel.removeFriendRequestFromHisDocument(currentFriendRequest!!)
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        //Update ui
                        othersProfileActivityViewModel.removeFriendRequestFromMyDocument(
                            currentFriendRequest!!
                        ).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                deleteNotification(auth.currentUser?.uid.toString(), notificationId)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    task2.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task1.exception?.message,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

    private fun createFriendshipBetweenMeAndHim(
        notificationId: String,
        meAsFriend: Friend,
        himAsFriend: Friend
    ) {
        othersProfileActivityViewModel
            .createFriendshipBetweenMeAndHim(meAsFriend, himAsFriend)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    deleteNotification(auth.currentUser?.uid.toString(), notificationId)
                    deleteFriendRequestFromMeAndHim(notificationId)
                }
            }
    }

    private fun createFollowingRelationBetweenMeAndHim() {

    }

    private fun deleteNotification(notifiedId: String, notificationId: String) {
        notificationsFragmentViewModel.deleteNotificationById(notifiedId, notificationId)
    }

    private fun navigateToPost(
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
    ) {
        val intent = Intent(requireContext(), PostViewerActivity::class.java)
        intent.putExtra("postPublisherId", postPublisherId)
        intent.putExtra("postId", postId)
        intent.putExtra("firstCollectionType", firstCollectionType)
        intent.putExtra("creatorReferenceId", creatorReferenceId)
        intent.putExtra("secondCollectionType", secondCollectionType)
        startActivity(intent)
    }

    private fun navigateToComment(
        postPublisherId: String,
        postId: String,
        commentPosition: Int,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String
        ) {
        val intent = Intent(requireContext(), PostViewerActivity::class.java)
        intent.putExtra("postPublisherId", postPublisherId)
        intent.putExtra("postId", postId)
        intent.putExtra("firstCollectionType", firstCollectionType)
        intent.putExtra("creatorReferenceId", creatorReferenceId)
        intent.putExtra("secondCollectionType", secondCollectionType)
        intent.putExtra("commentPosition", commentPosition)
        startActivity(intent)
    }

    private fun showDeleteNotificationConfirmationDialog(notification: Notification) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_notification_dialog)

        val deleteButton = dialog.findViewById(R.id.deleteNotificationTextView) as TextView
        val cancelButton = dialog.findViewById(R.id.cancelNotificationDeletionTextView) as TextView
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        deleteButton.setOnClickListener {
            deleteNotification(notification.notifiedId.toString(), notification.id.toString())
            dialog.dismiss()
        }
        dialog.show()

    }


}