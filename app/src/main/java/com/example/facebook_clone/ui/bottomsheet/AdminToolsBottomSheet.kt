package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.AdminToolsListener
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.group.Member
import com.example.facebook_clone.model.group.SemiGroup
import com.example.facebook_clone.viewmodel.GroupsViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.admin_tools_bottom_sheet.*
import kotlinx.android.synthetic.main.profile_images_bottom_sheet_layout.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

private const val IMAGE_REQUEST_CODE = 963
private const val TAG = "AdminToolsBottomSheet"
class AdminToolsBottomSheet(
    private val adminToolsListener: AdminToolsListener,
    private val group: Group
) : BottomSheetDialogFragment() {
    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null
    private val groupsViewModel by viewModel<GroupsViewModel>()
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
        uploadCoverLayout.setOnClickListener {
                //1 get image from gallery
                val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
                imageIntent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(imageIntent, "Choose an image"),
                    IMAGE_REQUEST_CODE
                )
        }

        leaveGroupLayout.setOnClickListener{
                val groupMembers = group.members.orEmpty()
                if (groupMembers.isNotEmpty()){
                    val member = groupMembers.first { member -> currentUserId == member.id }
                    showLeaveGroupDialog(group, member)
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
            val imagePath = data.data!!

            try {
                //I know it is not a profile image
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imagePath)
                groupsViewModel.uploadGroupCoverToCloudStorage(bitmap, group.id.orEmpty())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                                progressDialog?.dismiss()
                                uploadCoverImageToGroupCollection(photoUrl.toString())
//                                dismiss()
                            }
                        }
                    }
                //imageView.setImageBitmap(bitmap)
            } catch (ex: IOException) {
                Log.e(TAG, "onActivityResult: ${ex.message}", ex)
            }
        }
    }

    private fun uploadCoverImageToGroupCollection(photoUrl: String) {
        groupsViewModel.addCoverImageToUserCollection(photoUrl, group.id.orEmpty())
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    updateCoverImageToUserGroups(photoUrl)
                }
                else{
                    Utils.toastMessage(requireContext(), task.exception?.message.toString())
                }
            }
    }

    private fun updateCoverImageToUserGroups(newGroupCoverUrl: String) {
        val member = group.admins.orEmpty().first()
        val semiGroup = SemiGroup(
            id = group.id,
            name = group.name,
            coverUrl = group.coverImageUrl
        )
        groupsViewModel.deleteGroupFromUserGroups(member, semiGroup).addOnCompleteListener { task ->
            if (task.isSuccessful){
                semiGroup.coverUrl = newGroupCoverUrl
                groupsViewModel.addGroupToUserGroups(member.id.orEmpty(), semiGroup)
            }
            else{
                Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLeaveGroupDialog(group: Group, member: Member) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
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
                            Toast.makeText(requireContext(), "You left :(", Toast.LENGTH_SHORT).show()
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