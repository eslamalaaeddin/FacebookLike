package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.NewsFeedTabsAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_news_feed.*

class NewsFeedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_feed)
        setUpTabs()

        searchIconImageView.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpTabs() {
       val tabsAdapter = NewsFeedTabsAdapter(supportFragmentManager)
        viewPager.adapter = tabsAdapter
        newsFeedTabLayout.setupWithViewPager(viewPager)

        newsFeedTabLayout.getTabAt(0)?.setIcon(R.drawable.ic_news_feed)
        newsFeedTabLayout.getTabAt(1)?.setIcon(R.drawable.ic_group)
        newsFeedTabLayout.getTabAt(2)?.setIcon(R.drawable.ic_alarm)
        newsFeedTabLayout.getTabAt(3)?.setIcon(R.drawable.ic_menu)

    }
}