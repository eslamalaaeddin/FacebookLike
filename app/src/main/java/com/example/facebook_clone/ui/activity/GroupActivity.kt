package com.example.facebook_clone.ui.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.AdminToolsListener
import com.example.facebook_clone.ui.bottomsheet.AdminToolsBottomSheet
import com.example.facebook_clone.ui.bottomsheet.InviteMembersBottomSheet
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_group.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupActivity : AppCompatActivity(), AdminToolsListener {
    private val groupsViewModel by viewModel<GroupsViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val auth: FirebaseAuth by inject()
    private val currentUserId = auth.currentUser?.uid.toString()
    private val picasso = Picasso.get()
    private var groupName = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        upButtonImageView.setOnClickListener { finish() }

        val groupId = intent.getStringExtra("groupId").orEmpty()

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
            }
        }

        adminBadgeImageView.setOnClickListener {
            showAdminToolsBottomSheet(groupId)
        }

        inviteFriendButton.setOnClickListener {
            val inviteMembersBottomSheet = InviteMembersBottomSheet(groupId, groupName)
            inviteMembersBottomSheet.show(supportFragmentManager, inviteMembersBottomSheet.tag)
        }

        showGroupMembersLayout.setOnClickListener {
            Toast.makeText(this, "Islam love Alaa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAdminToolsBottomSheet(groupId: String?) {
        val adminToolsBottomSheet =
            AdminToolsBottomSheet(this, groupId.orEmpty(), groupName = "Friends4Ever")
        adminToolsBottomSheet.show(supportFragmentManager, adminToolsBottomSheet.tag)
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