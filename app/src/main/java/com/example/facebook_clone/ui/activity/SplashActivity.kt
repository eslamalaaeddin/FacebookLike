package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.example.facebook_clone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.android.ext.android.inject

private const val TAG = "SplashActivity"
class SplashActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by inject()
    private val currentUser: FirebaseUser? = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (currentUser == null) {
                navigateToRecentUsersActivity()
            } else {
//                navigateToNewsFeedActivity()
                navigateToProfileActivity()
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            //navigateToRecentUsersActivity()
            //finish()

        }, 3000)
    }

    private fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()
    }

    private fun navigateToRecentUsersActivity() {
        val intent = Intent(this, RecentUsersActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun navigateToNewsFeedActivity() {
        val intent = Intent(this, NewsFeedActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
}