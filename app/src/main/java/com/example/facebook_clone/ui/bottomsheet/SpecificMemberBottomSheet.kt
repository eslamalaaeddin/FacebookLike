package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.ui.activity.OthersProfileActivity
import com.example.facebook_clone.ui.activity.ProfileActivity
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_testing.*
import kotlinx.android.synthetic.main.specific_members_bottom_sheet_layout.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class SpecificMemberBottomSheet(private val group: Group, private val member: Member) : BottomSheetDialogFragment() {
    private val auth : FirebaseAuth by inject()
    private val groupsViewModel by viewModel<GroupsViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.specific_members_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upButtonImageView.setOnClickListener { dismiss() }

        memberNameTextView.text = member.name.orEmpty()

        viewMemberProfileLayout.setOnClickListener{
            navigateToMemberProfile(member.id.orEmpty())
        }

        blockMemberLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Not implemented yet.", Toast.LENGTH_SHORT).show()
        }

        removeMemberLayout.setOnClickListener {
            showRemoveMemberDialog()
        }
    }

    private fun navigateToMemberProfile(memberId: String) {
        if (memberId == auth.currentUser?.uid.toString()) {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        } else {
            val intent = Intent(requireContext(), OthersProfileActivity::class.java)
            intent.putExtra("userId", memberId)
            startActivity(intent)
        }
    }

    private fun showRemoveMemberDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.remove_member_dialog)

        val res: Resources = resources
        val text: String =
            java.lang.String.format(res.getString(R.string.remove_member_info), member.name.orEmpty())


        val cancelButton = dialog.findViewById(R.id.cancelMemberRemovingTextView) as TextView
        val removingInfo = dialog.findViewById(R.id.removeMemberInfoTextView) as TextView
        val removeButton = dialog.findViewById(R.id.removeMemberTextView) as TextView

        removingInfo.text = text

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        removeButton.setOnClickListener {
            groupsViewModel.deleteMemberFromGroup(group.id.orEmpty(), member).addOnCompleteListener {
                if (it.isSuccessful){
                    val semiGroup = SemiGroup(
                        id = group.id,
                        name = group.name,
                        coverUrl = group.coverImageUrl
                    )

                    groupsViewModel.deleteGroupFromUserGroups(member, semiGroup).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(requireContext(), "${member.name} removed successfully", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                else{
                    Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }
        dialog.show()

    }
}