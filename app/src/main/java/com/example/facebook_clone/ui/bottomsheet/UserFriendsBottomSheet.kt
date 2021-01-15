package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.FriendsAdapter
import com.example.facebook_clone.adapter.InvitedFriendsAdapter
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.model.user.friend.Friend
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.invite_members_bottom_sheet.*
import kotlinx.android.synthetic.main.user_friends_bottom_sheet.*
import kotlinx.android.synthetic.main.user_friends_bottom_sheet.searchForFriendsEditText
import kotlinx.android.synthetic.main.user_friends_bottom_sheet.upButtonImageView

class UserFriendsBottomSheet(
    private var friends: List<Friend>,
    private val friendClickListener: FriendClickListener
) : BottomSheetDialogFragment() {
    private lateinit var friendsAdapter: FriendsAdapter

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
        return inflater.inflate(R.layout.user_friends_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upButtonImageView.setOnClickListener { dismiss() }

        friendsAdapter = FriendsAdapter(friends, friendClickListener, "linear")
        userFriendsRecyclerView.adapter = friendsAdapter

        searchForFriendsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editableText: Editable?) {
                var currentFriends = friends
                currentFriends = currentFriends.filter { friend ->
                    friend.name.orEmpty().toLowerCase().contains(editableText.toString().toLowerCase())
                }
                friendsAdapter = FriendsAdapter(currentFriends, friendClickListener, "linear")
                userFriendsRecyclerView.adapter = friendsAdapter
            }
        })


    }
}