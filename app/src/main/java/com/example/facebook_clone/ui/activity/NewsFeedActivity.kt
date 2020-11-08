package com.example.facebook_clone.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.facebook_clone.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_news_feed.*

class NewsFeedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_feed)

        hello.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
    }
}