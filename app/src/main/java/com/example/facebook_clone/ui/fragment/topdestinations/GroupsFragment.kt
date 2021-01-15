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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.GroupsAdapter
import com.example.facebook_clone.helper.listener.GroupsItemListener
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.model.group.SemiGroupDocument
import com.example.facebook_clone.ui.activity.GroupActivity
import com.example.facebook_clone.viewmodel.fragment.GroupsFragmentViewModel
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.example.facebook_clone.viewmodel.activity.ProfileActivityViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_groups.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "GroupsFragment"
class GroupsFragment : Fragment(R.layout.fragment_groups), GroupsItemListener {
    private val groupsFragmentViewModel by viewModel<GroupsFragmentViewModel>()
    private val groupViewModel by viewModel<GroupsViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val auth: FirebaseAuth by inject()
    private val currentUser = auth.currentUser
    private lateinit var groupCreatorName: String
    private lateinit var groupCreatorToken: String
    private lateinit var groupCreatorProfileImageUrl: String
    private val currentUserId = auth.currentUser?.uid.toString()
    private lateinit var groupsAdapter: GroupsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        userLiveData?.observe(viewLifecycleOwner, {
            groupCreatorName = it.name.orEmpty()
            groupCreatorToken = it.token.orEmpty()
            groupCreatorProfileImageUrl = it.profileImageUrl.orEmpty()
        })
        addGroupFab.setOnClickListener { showCreateGroupDialog() }

        val allGroupsTask = groupViewModel.getAllGroups(currentUserId)

        allGroupsTask.addOnCompleteListener {
            if (it.isSuccessful){
                val groups = it.result?.toObject(SemiGroupDocument::class.java)?.groups.orEmpty()
                groupsAdapter = GroupsAdapter(groups, this)
                groupsRecyclerView.adapter = groupsAdapter
                val layoutManager = LinearLayoutManager(requireContext())
                val dividerItemDecoration = DividerItemDecoration(
                    groupsRecyclerView.context,
                    layoutManager.orientation
                )
                groupsRecyclerView.addItemDecoration(dividerItemDecoration)
            }
        }

    }


    private fun createGroup(groupName: String): Group {
        val admin = Member(
            id = currentUserId,
            name = groupCreatorName,
            imageUrl = groupCreatorProfileImageUrl
        )
        return Group(
            name = groupName,
            admins = listOf(admin),
        )
    }

    private fun showCreateGroupDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.create_group_dialog_layout)

        val cancelButton = dialog.findViewById(R.id.cancelGroupCreationButton) as TextView
        val createButton = dialog.findViewById(R.id.createGroupButton) as TextView
        val groupNameEditText = dialog.findViewById(R.id.groupNameEditText) as TextInputLayout
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        createButton.setOnClickListener {
            val groupName = groupNameEditText.editText?.text.toString()
            if (groupName.isEmpty()) {
                groupNameEditText.error = "Enter group name first"
            } else {
                val group = createGroup(groupName)
                groupsFragmentViewModel.createGroup(group).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val member = Member(
                            id = currentUserId,
                            name = groupCreatorName,
                            imageUrl = groupCreatorProfileImageUrl,
                            blocked = false
                        )
                        groupViewModel.addMemberToGroup(member, group.id.toString())
                        val semiGroup = SemiGroup(
                            group.id.toString(),
                            group.name.toString(),
                            group.coverImageUrl.toString()
                        )
                        addGroupToUserGroups(currentUserId, semiGroup)
                        dialog.dismiss()
                    }
                }
            }

        }
        dialog.show()

    }

    private fun addGroupToUserGroups(userId: String, semiGroup: SemiGroup){
        groupViewModel.addGroupToUserGroups(userId, semiGroup).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(requireContext(), "${semiGroup.name} created successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), GroupActivity::class.java).putExtra("groupId", semiGroup.id))
            }
            else{
                Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onGroupClicked(groupId: String) {
        val intent = Intent(requireContext(), GroupActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
    }


}