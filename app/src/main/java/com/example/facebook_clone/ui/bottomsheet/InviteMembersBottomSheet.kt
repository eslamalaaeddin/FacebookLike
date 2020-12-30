package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.InvitedFriendsAdapter
import com.example.facebook_clone.helper.listener.InvitedFriendsListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.invite_members_bottom_sheet.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class InviteMembersBottomSheet(private val groupId: String, private val groupName: String): BottomSheetDialogFragment(), InvitedFriendsListener {
    private val auth: FirebaseAuth by inject()
    private lateinit var invitedFriendsAdapter : InvitedFriendsAdapter
    private val currentUserId :String = auth.currentUser?.uid.toString()
    private lateinit var notificationsHandler: NotificationsHandler
    private var currentUserName : String? = null
    private var currentUserImageUrl : String? = null
    private val othersProfileActivityViewModel by viewModel<OthersProfileActivityViewModel>()
    private val notificationsFragmentViewModel by viewModel<NotificationsFragmentViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
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
        return inflater.inflate(R.layout.invite_members_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        upButtonImageView.setOnClickListener {
            dismiss()
        }
        notificationsHandler = NotificationsHandler(
            othersProfileActivityViewModel = othersProfileActivityViewModel,
            notificationsFragmentViewModel = notificationsFragmentViewModel
        )

        val myLiveData = profileActivityViewModel.getMe(currentUserId)
        myLiveData?.observe(viewLifecycleOwner){
            currentUserName = it.name
            currentUserImageUrl = it.profileImageUrl
            invitedFriendsAdapter = InvitedFriendsAdapter(groupName = groupName, it.friends.orEmpty(), this)
            friendsToBeInvitedRecyclerView.adapter = invitedFriendsAdapter
        }

    }

    override fun onInviteButtonClicked(invitedId: String, groupName: String) {
        notificationsHandler.notifiedId = invitedId
        notificationsHandler.notifierId = currentUserId
        notificationsHandler.notificationType = "groupInvitation"
        notificationsHandler.notifierImageUrl = currentUserImageUrl
        notificationsHandler.notifierName  = currentUserName
        notificationsHandler.groupId = groupId
        notificationsHandler.groupName = groupName
        //get the user live data
        //get its token
        //notify him(notification in fragment || notification with sound )
//        notificationsHandler.notifiedToken = token
        //NOTIFICATION IN FRAGMENT
        notificationsHandler.handleNotificationCreationAndFiring()
    }


}