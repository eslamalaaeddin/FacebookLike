package com.example.facebook_clone.ui.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.listener.AdminToolsListener
import com.example.facebook_clone.model.group.Group
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.admin_tools_bottom_sheet.*
import org.koin.android.ext.android.inject

private const val TAG = "AdminToolsBottomSheet"
class AdminToolsBottomSheet(
    private val adminToolsListener: AdminToolsListener,
    private val group: Group
) : BottomSheetDialogFragment() {
    private val auth: FirebaseAuth by inject()
    private val currentUserId: String = auth.currentUser?.uid.toString()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_tools_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addMemberLayout.setOnClickListener { showInviteMembersBottomSheet() }
        membersLayout.setOnClickListener { showMembersBottomSheet(group) }
    }

    private fun showInviteMembersBottomSheet(){

//        Toast.makeText(requireContext(), groupId, Toast.LENGTH_SHORT).show()
//        Toast.makeText(requireContext(), groupName, Toast.LENGTH_SHORT).show()
        val inviteMembersBottomSheet = InviteMembersBottomSheet(group)
        inviteMembersBottomSheet.show(activity?.supportFragmentManager!!, inviteMembersBottomSheet.tag)
    }

    private fun showMembersBottomSheet(group: Group) {
        val membersBottomSheet = MembersBottomSheet(group)
        membersBottomSheet.show(activity?.supportFragmentManager!!, membersBottomSheet.tag)
    }
}