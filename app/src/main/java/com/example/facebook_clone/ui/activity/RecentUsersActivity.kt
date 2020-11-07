package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.activity_recent_users.*

class RecentUsersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_users)

        createNewFacebookAccount.setOnClickListener {navigateToUserNameFragment()}
        logIntoAnotherAccount.setOnClickListener { navigateToLoginFragment() }


    }
    //false ==> new user
    //true ==> existing user
    private fun navigateToUserNameFragment(){
        RegisteringActivity.open(this,false)
        finish()
    }

    private fun navigateToLoginFragment(){
        RegisteringActivity.open(this,true)
        finish()
    }
}