package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.activity_profile_picture.*

class ProfilePictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_picture)

        skipProfilePictureActivity.setOnClickListener {
            val intent = Intent(this, NewsFeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}