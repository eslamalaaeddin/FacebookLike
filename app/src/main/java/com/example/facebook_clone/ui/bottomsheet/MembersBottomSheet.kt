package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.GroupMembersAdapter
import com.example.facebook_clone.helper.listener.GroupMemberListener
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.group_members_bottom_sheet_layout.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "MembersBottomSheet"
class MembersBottomSheet(private val group: Group): BottomSheetDialogFragment(), GroupMemberListener {
    private val auth: FirebaseAuth by inject()
    private val currentUserId = auth.currentUser?.uid.toString()
    private val groupsViewModel by viewModel<GroupsViewModel>()
    private lateinit var membersAdapter : GroupMembersAdapter
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_members_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        upButtonImageView.setOnClickListener { dismiss() }
        //Get the group live data and
        val groupLiveData = groupsViewModel.getGroupLiveData(group.id.orEmpty())
        groupLiveData.observe(viewLifecycleOwner){
            val members = it.members.orEmpty()
            membersAdapter = GroupMembersAdapter(members, this)
            groupMemberRecyclerView.adapter = membersAdapter
        }
        
        adminImageView.setOnClickListener { Toast.makeText(requireContext(), group.id, Toast.LENGTH_SHORT).show() }
    }

    override fun onGroupMemberClicked(member: Member) {
        //open GroupMember
        val specificMemberBottomSheet = SpecificMemberBottomSheet(group, member)
        specificMemberBottomSheet.show(activity?.supportFragmentManager!!, specificMemberBottomSheet.tag)
    }
}