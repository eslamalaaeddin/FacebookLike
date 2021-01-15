package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.Utils.POST_FROM_GROUP
import com.example.facebook_clone.helper.listener.AdminToolsListener
import com.example.facebook_clone.helper.listener.GroupPostsCreatorListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.posthandler.GroupsActivityPostsHandler
import com.example.facebook_clone.helper.posthandler.OthersProfileActivityPostsHandler
import com.example.facebook_clone.helper.posthandler.ProfileActivityPostsHandler
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
//import com.example.facebook_clone.helper.posthandler.GroupsActivityPostsHandler
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.ui.bottomsheet.AdminToolsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.InviteMembersBottomSheet
import com.example.facebook_clone.ui.bottomsheet.MemberPostConfigurationBottomSheet
import com.example.facebook_clone.ui.bottomsheet.MembersBottomSheet
import com.example.facebook_clone.ui.dialog.ImageViewerDialog
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.profile_cover_bottom_sheet_layout.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "GroupActivity"

class GroupActivity : AppCompatActivity(), AdminToolsListener, PostListener,
    GroupPostsCreatorListener {
    private val groupsViewModel by viewModel<GroupsViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private val auth: FirebaseAuth by inject()
    private val currentUserId = auth.currentUser?.uid.toString()
    private var currentUserName = ""
    private var currentUserImageUrl = ""
    private var groupId = ""
    private var groupName = ""
    private var adminId = ""
    private var currentGroup: Group? = null
    private val picasso = Picasso.get()
    private lateinit var othersProfileActivityPostsHandler: OthersProfileActivityPostsHandler
    private lateinit var profileActivityPostsHandler: ProfileActivityPostsHandler
    private lateinit var groupsActivityPostsHandler: GroupsActivityPostsHandler
    private lateinit var profilePostsAdapter: ProfilePostsAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        othersProfileActivityPostsHandler = OthersProfileActivityPostsHandler(
            this,
            postViewModel,
            notificationsFragmentViewModel,
            othersProfileActivityViewModel
        )
        profileActivityPostsHandler =
            ProfileActivityPostsHandler("", this, postViewModel, profileActivityViewModel)

        upButtonImageView.setOnClickListener { finish() }

        groupId = intent.getStringExtra("groupId").orEmpty()

//        joinGroupButton.setOnClickListener {
//            val currentMember = Member(id = currentUserId, imageUrl = currentUserImageUrl, name = currentUserName)
//            val joinRequest = JoinRequest(requester = currentMember)
//            currentGroup?.let {currentGroup ->
//                groupsViewModel.sendJoinRequestToGroupAdmins(currentGroup, joinRequest).addOnCompleteListener { task ->
//                    if (task.isSuccessful){
//                        //Get admin id
//                        //Notify him sound i don't want to
//                        //Notify him
//                        val adminLiveData = profileActivityViewModel.getAnotherUser(currentGroup.admins.orEmpty().first().id.orEmpty())
//                        adminLiveData?.observe(this){admin ->
//                            val adminToken = admin.token.orEmpty()
//                           val notificationHandler =
//                               othersProfileActivityPostsHandler.buildNotificationHandlerForGroupJoinRequest(
//                                   notifierId = currentUserId,
//                                   notifierName = currentUserName,
//                                   notifierImageUrl = currentUserImageUrl,
//                                   notifiedId = admin.id.orEmpty(),
//                                   notifiedToken = adminToken,
//                                   group = currentGroup
//                               )
//                            notificationHandler.handleNotificationCreationAndFiring()
//                            adminLiveData.removeObservers(this)
//
//                        }
//                    }
//                    else{
//                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }

        val groupLiveData = groupsViewModel.getGroupLiveData(groupId)
        groupLiveData.observe(this) { group ->
            currentGroup = group
            groupName = group.name.toString()
            adminId = group.admins.orEmpty().first().id.orEmpty()

            if (group.coverImageUrl == null){
                groupCoverImageView.setImageResource(R.drawable.facebook_group_cover)
            }
            else{
                picasso.load(group.coverImageUrl).into(groupCoverImageView)
            }
            groupsActivityPostsHandler = GroupsActivityPostsHandler(
                this,
                group,
                postViewModel,
                profileActivityViewModel,
                notificationsFragmentViewModel,
                othersProfileActivityViewModel
            )
            val groupMembers = group.members.orEmpty()
            for ((i, member) in groupMembers.withIndex()) {
                if (member.id.orEmpty() == currentUserId){
                    updateUIForMemberUser()
                    break
                }
                else if (member.id.orEmpty() != currentUserId ) {
                   // Toast.makeText(this, "Not Member ${member.id}", Toast.LENGTH_SHORT).show()
                    updateUIForNonMemberUser()
//                    val groupJoinRequests = group.joinRequests.orEmpty()
//                    if (groupJoinRequests.isNotEmpty()){
//                        for (joinReq in groupJoinRequests){
//                            //You made a request
//                            if (joinReq.requester?.id.orEmpty() == currentUserId){
//                                canceljoiningGroupButton.visibility = View.VISIBLE
//                                joinGroupButton.visibility = View.GONE
//                            }
//                        }
//                    }
                }
            }

            groupCoverImageView.setOnClickListener {
                val imageViewerDialog = ImageViewerDialog()
                imageViewerDialog.show(supportFragmentManager, "signature")
                imageViewerDialog.setMediaUrl(group.coverImageUrl.orEmpty())
            }


            groupNameTextView.text = group.name
            groupMembersCountTextView.text = "${groupMembers.size } Members"
            if (groupMembers.size >= 2) {
                val firstMember = groupMembers[0]
                val secondMember = groupMembers[1]

                picasso.load(firstMember.imageUrl).into(thirdMemberPlaceHolder)
                picasso.load(secondMember.imageUrl).into(secondMemberPlaceHolder)
            }
        }

        val currentUserLiveData = profileActivityViewModel.getMe(currentUserId)
        currentUserLiveData?.let {
            it.observe(this) { user ->
                picasso.load(user.profileImageUrl).into(smallUserImageView)
                currentUserName = user.name.orEmpty()
                currentUserImageUrl = user.profileImageUrl.orEmpty()

                val groupPostsLiveData = groupsViewModel.getGroupPostsLiveData(groupId)
                groupPostsLiveData.observe(this) { posts ->
                    posts?.let {

                        profilePostsAdapter =
                            ProfilePostsAdapter(
                                auth,
                                posts,
                                this, //was groupPostsHandler
                                currentUserName,
                                currentUserImageUrl,
                                null,
                                currentUserId,
                                POST_FROM_GROUP
                            )
                        groupPostsRecyclerView.adapter = profilePostsAdapter
                    }
                }
            }


        }

        adminBadgeImageView.setOnClickListener { currentGroup?.let { g -> showAdminToolsBottomSheet(group = g) } }

        inviteFriendButton.setOnClickListener {
            val inviteMembersBottomSheet = currentGroup?.let { it1 -> InviteMembersBottomSheet(it1) }
            inviteMembersBottomSheet?.show(supportFragmentManager, inviteMembersBottomSheet.tag)
        }

        showGroupMembersLayout.setOnClickListener {
            currentGroup?.let { group ->
                showMembersBottomSheet(group)
            }
        }

        smallUserImageView.setOnClickListener { navigateToUserProfile() }

        whatIsInYourMindButton.setOnClickListener { showPostCreatorDialog() }

        moreImageView.setOnClickListener {
            //Should create bottom sheet that contains leave group nad info, but let it for now as it is
            currentGroup?.let {group ->
                val groupMembers = group.members.orEmpty()
                if (groupMembers.isNotEmpty()){
                    val member = groupMembers.first { member -> currentUserId == member.id }
                    showLeaveGroupDialog(group, member)
                }
            }
        }
    }

    private fun showMembersBottomSheet(group: Group) {
        val membersBottomSheet = MembersBottomSheet(group)
        membersBottomSheet.show(supportFragmentManager, membersBottomSheet.tag)
    }

    private fun updateUIForNonMemberUser() {
        joinGroupButton.visibility = View.VISIBLE
        adminBadgeImageView.visibility = View.GONE
        whatIsInYourMindButton.visibility = View.GONE
        smallUserImageView.visibility = View.GONE
        moreImageView.visibility = View.VISIBLE
    }

    private fun updateUIForMemberUser() {
        joinGroupButton.visibility = View.GONE
        whatIsInYourMindButton.visibility = View.VISIBLE
        smallUserImageView.visibility = View.VISIBLE
    }

    private fun showAdminToolsBottomSheet(group: Group) {
        val adminToolsBottomSheet =
            AdminToolsBottomSheet(this, group)
        adminToolsBottomSheet.show(supportFragmentManager, adminToolsBottomSheet.tag)
    }

    private fun navigateToUserProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId", currentUserId)
        startActivity(intent)
    }

    private fun showPostCreatorDialog() {
        val postCreatorDialog = PostCreatorDialog(POST_FROM_GROUP, groupId, groupName, this)
        postCreatorDialog.show(supportFragmentManager, "signature")
        postCreatorDialog
            .setUserNameAndProfileImageUrl(
                currentUserName,
                currentUserImageUrl
            )
    }

    override fun onMemberRequestClicked() {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onMembersClicked() {

    }

    override fun onLeaveClicked() {
    }

    override fun onAddMemberClicked() {
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
        if (!reacted) {
            if (post.publisherId == interactorId) {
                profileActivityPostsHandler.onReactButtonClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    reacted,
                    currentReact,
                    postPosition
                )
            } else {
                val userToBeNotified =
                    profileActivityViewModel.getAnotherUser(post.publisherId.orEmpty())
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
        } else {
            othersProfileActivityPostsHandler.onReactButtonClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                postPosition
            )
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
        if (!reacted) {
            if (post.publisherId == interactorId) {
                profileActivityPostsHandler.onReactButtonLongClicked(
                    post,
                    interactorId,
                    interactorName,
                    interactorImageUrl,
                    reacted,
                    currentReact,
                    postPosition
                )
            } else {
                val userToBeNotified =
                    profileActivityViewModel.getAnotherUser(post.publisherId.orEmpty())
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
        } else {
            othersProfileActivityPostsHandler.onReactButtonLongClicked(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                reacted,
                currentReact,
                postPosition
            )
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
        Toast.makeText(this, "No Group Shares", Toast.LENGTH_SHORT).show()
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
        othersProfileActivityPostsHandler.handleMediaClicks(mediaUrl)
    }

    override fun onPostMoreDotsClicked(interactorId: String, post: Post, shared: Boolean?) {
        if (interactorId == adminId) {
            showPostAdminTools(post)
        } else {
            if (interactorId == post.publisherId) {
                showMemberPostTools(post)
            }else{
                showMemberPostTools(null)
            }
        }
    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {

    }

    private fun showPostAdminTools(post: Post){
        val memberPostConfigurationBottomSheet = MemberPostConfigurationBottomSheet(post, "admin")
        memberPostConfigurationBottomSheet.also {
            it.show(supportFragmentManager, it.tag)
        }
    }

    private fun showMemberPostTools(post: Post?){
            val memberPostConfigurationBottomSheet = MemberPostConfigurationBottomSheet(post, "member")
            memberPostConfigurationBottomSheet.also {
                it.show(supportFragmentManager, it.tag)
            }
    }

    override fun onGroupPostCreated(post: Post) {
        currentGroup?.let { group ->
            val groupMembers = group.members.orEmpty()
            for (member in groupMembers) {
                if (member.id != post.publisherId) {
                    val memberLiveData =
                        profileActivityViewModel.getAnotherUser(member.id.orEmpty())
                    memberLiveData?.observe(this) { user ->
                        val userToken = user.token
                        val notHandler =
                            groupsActivityPostsHandler.buildNotificationHandlerForGroupPostCreation(
                                post = post,
                                interactorId = currentUserId,
                                interactorName = currentUserName,
                                interactorImageUrl = currentUserImageUrl,
                                notifiedToken = userToken.orEmpty(),
                            )
                        notHandler.notifiedId = member.id.orEmpty()
                        notHandler.handleNotificationCreationAndFiring()
                        memberLiveData.removeObservers(this)
                    }
                }
            }
        }
    }

    private fun showLeaveGroupDialog(group: Group, member: Member) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.leave_group_layout)

        val cancelButton = dialog.findViewById(R.id.cancelLeavingGroupTextView) as TextView
        val leaveButton = dialog.findViewById(R.id.leaveGroupTextView) as TextView

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        leaveButton.setOnClickListener {
            groupsViewModel.deleteMemberFromGroup(group.id.orEmpty(), member).addOnCompleteListener {
                if (it.isSuccessful){
                    val semiGroup = SemiGroup(
                        id = group.id,
                        name = group.name,
                        coverUrl = group.coverImageUrl
                    )
                    groupsViewModel.deleteGroupFromUserGroups(member, semiGroup).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this, "You left :(", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                else{
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }
        dialog.show()

    }

}