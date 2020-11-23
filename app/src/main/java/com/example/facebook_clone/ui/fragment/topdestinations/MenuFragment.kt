package com.example.facebook_clone.ui.fragment.topdestinations

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.facebook_clone.R
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.ui.activity.ProfileActivity
import com.example.facebook_clone.ui.activity.RecentUsersActivity
import com.example.facebook_clone.viewmodel.ProfileActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_menu.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MenuFragment: Fragment(R.layout.fragment_menu) {
    private val auth: FirebaseAuth by inject()
    private val profileActivityViewModel by viewModel<ProfileActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userLiveData = profileActivityViewModel.getMe(auth.currentUser?.uid.toString())
        userLiveData?.observe(viewLifecycleOwner, {
            if (it.profileImageUrl != null) {
                Picasso.get().load(it.profileImageUrl).into(userImageView)
            }
            userNameTextView.text = it.name
        })
        seeYourProfileLayout.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        logoutLayout.setOnClickListener{
            auth.signOut()
            activity?.finish()
            startActivity(Intent(requireContext(), RecentUsersActivity:: class.java))
        }

    }
}