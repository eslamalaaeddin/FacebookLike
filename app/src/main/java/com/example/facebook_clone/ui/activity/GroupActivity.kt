package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.ProfilePostsAdapter
import com.example.facebook_clone.helper.Utils.POST_FROM_GROUP
import com.example.facebook_clone.helper.listener.AdminToolsListener
import com.example.facebook_clone.ui.bottomsheet.AdminToolsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.InviteMembersBottomSheet
import com.example.facebook_clone.ui.dialog.PostCreatorDialog
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_group.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "GroupActivity"
class GroupActivity : AppCompatActivity(), AdminToolsListener {
    private val groupsViewModel by viewModel<GroupsViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val auth: FirebaseAuth by inject()
    private val currentUserId = auth.currentUser?.uid.toString()
    private var currentUserName = ""
    private var currentUserImageUrl = ""
    private val picasso = Picasso.get()
    private var groupId = ""
    private var groupName = ""
//    private val postHandler = PostHandler(this)
    private lateinit var profilePostsAdapter: ProfilePostsAdapter
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        upButtonImageView.setOnClickListener { finish() }

        groupId = intent.getStringExtra("groupId").orEmpty()
        
        val groupPostsLiveData = groupsViewModel.getGroupPostsLiveData(groupId)
        groupPostsLiveData.observe(this){posts ->
            posts?.let {
//                profilePostsAdapter =
//                    ProfilePostsAdapter(auth, posts, postHandler, currentUserName, currentUserImageUrl, null,currentUserId )
//                groupPostsRecyclerView.adapter = profilePostsAdapter
            }
        }

        val groupLiveData = groupsViewModel.getGroupLiveData(groupId)
        groupLiveData.observe(this) { group ->
            groupName = group.name.toString()
            val groupMembers = group.members.orEmpty()
            picasso.load(group.coverImageUrl).into(groupCoverImageView)
            groupNameTextView.text = group.name
            groupMembersCountTextView.text = "${groupMembers.size + 2} Members"
            if (groupMembers.size >= 2){
                val firstMember = groupMembers[0]
                val secondMember = groupMembers[1]

                picasso.load(firstMember.imageUrl).into(firstMemberPlaceHolder)
                picasso.load(secondMember.imageUrl).into(secondMemberPlaceHolder)
            }
        }

        val currentUserLiveData = profileActivityViewModel.getMe(currentUserId)
        currentUserLiveData?.let {
            it.observe(this) {user ->
                picasso.load(user.profileImageUrl).into(smallUserImageView)
                currentUserName = user.name.orEmpty()
                currentUserImageUrl = user.profileImageUrl.orEmpty()
            }
        }

        adminBadgeImageView.setOnClickListener {showAdminToolsBottomSheet(groupId)}

        inviteFriendButton.setOnClickListener {
            val inviteMembersBottomSheet = InviteMembersBottomSheet(groupId, groupName)
            inviteMembersBottomSheet.show(supportFragmentManager, inviteMembersBottomSheet.tag)
        }

        showGroupMembersLayout.setOnClickListener {
            Toast.makeText(this, "Islam love Alaa", Toast.LENGTH_SHORT).show()
        }

        smallUserImageView.setOnClickListener { navigateToUserProfile()}

        whatIsInYourMindButton.setOnClickListener {showPostCreatorDialog()}
    }

    private fun showAdminToolsBottomSheet(groupId: String?) {
        val adminToolsBottomSheet =
            AdminToolsBottomSheet(this, groupId.orEmpty(), groupName = "Friends4Ever")
        adminToolsBottomSheet.show(supportFragmentManager, adminToolsBottomSheet.tag)
    }

    private fun navigateToUserProfile(){
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId", currentUserId)
        startActivity(intent)
    }

    private fun showPostCreatorDialog(){
        val postCreatorDialog = PostCreatorDialog(POST_FROM_GROUP, groupId)
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

}