package com.example.facebook_clone.ui.bottomsheet

import android.app.Dialog
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.example.facebook_clone.R
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.followed.Followed
import com.example.facebook_clone.model.user.follower.Follower
import com.example.facebook_clone.model.user.friend.Friend
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.user_relations_bottom_sheet.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserRelationsBottomSheet(private val friendId: String): BottomSheetDialogFragment() {
    private val othersViewModel by viewModel<OthersProfileActivityViewModel>()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()
    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
    private var meAsFriend: Friend? = null
    private var currentFriend: Friend? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.user_relations_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        myLiveData?.observe(viewLifecycleOwner, {user ->
            currentUser = user
            val friends = user.friends.orEmpty()
            for (friend in friends){
                if (friend.id == friendId){
                    currentFriend = friend
                    break
                }
            }

            currentUser.followings?.let {followings ->
                if (followings.isEmpty()) {
                    followUserLayout.visibility = View.VISIBLE
                    unFollowUserLayout.visibility = View.GONE
                }

                else {
                    for (following in followings) {
                        if (following.id == friendId) {
                            followUserLayout.visibility = View.GONE
                            unFollowUserLayout.visibility = View.VISIBLE
                            break
                        }
                    }
                }


            }
            currentUser.friends?.let { friends ->
                if (friends.isEmpty()) {
                    unFriendLayout.visibility = View.GONE
                }

                else {
                    for (friend in friends) {
                        if (friend.id == friendId) {
                            unFriendLayout.visibility = View.VISIBLE
                            meAsFriend = friend
                            break
                        }
                    }
                }
            }
        })



        followUserLayout.setOnClickListener {
            val meAsAFollower = Follower(currentUser.id, currentUser.name, currentUser.profileImageUrl)
            //following == followed
            val himAsAFollowing = Followed(currentFriend?.id, currentFriend?.name, currentFriend?.imageUrl)
            othersViewModel.addMeAsAFollowerToHisDocument(currentFriend?.id.toString(), meAsAFollower)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        othersViewModel.addHimAsAFollowingToMyDocument(currentUser.id.toString(), himAsAFollowing)
                    }
                }
            dismiss()
        }

        unFollowUserLayout.setOnClickListener {
            val meAsAFollower = Follower(currentUser.id, currentUser.name, currentUser.profileImageUrl)
            //following == followed
            val himAsAFollowing = Followed(currentFriend?.id, currentFriend?.name, currentFriend?.imageUrl)
            othersViewModel.deleteMeAsAFollowerFromHisDocument(currentFriend?.id.toString(), meAsAFollower)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        othersViewModel.deleteHimAsAFollowingFromMyDocument(currentUser.id.toString(), himAsAFollowing)
                    }
                }
            dismiss()
        }

        unFriendLayout.setOnClickListener {
            showDeleteFriendDialog()
        }
    }

    private fun showDeleteFriendDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.delete_friend_dialog_layout)

        val cancelButton = dialog.findViewById(R.id.cancelFriendDeletion) as TextView
        val deleteButton = dialog.findViewById(R.id.confirmFriendDeletion) as TextView
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        deleteButton.setOnClickListener {
            currentFriend?.let { currentFriend ->
                othersViewModel.also {
                    it.deleteFriendFromFriends(currentFriend, auth.currentUser?.uid.toString())
                    meAsFriend?.let {meAsFriend ->
                        it.deleteFriendFromFriends(meAsFriend, currentFriend.id.toString())
                    }

                }
            }
            dialog.dismiss()
        }
        dialog.show()

    }
}