package com.example.facebook_clone.ui.fragment.topdestinations

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.facebook_clone.R
import com.example.facebook_clone.ui.activity.ProfileActivity
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment: Fragment(R.layout.fragment_menu) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seeYourProfileLayout.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}