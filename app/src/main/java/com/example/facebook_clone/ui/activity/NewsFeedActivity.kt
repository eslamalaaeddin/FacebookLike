package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.NewsFeedTabsAdapter
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.viewmodel.NewsFeedActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_news_feed.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsFeedActivity : AppCompatActivity() {
    private val newsFeedActivityViewModel by viewModel<NewsFeedActivityViewModel>()
    private var fromRecentUsersActivity = false
    private val auth: FirebaseAuth by inject()
    private var email:String? = null
    private var password:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_feed)
        setUpTabs()
         email = intent.getStringExtra("email").toString()
         password = intent.getStringExtra("password").toString()
        fromRecentUsersActivity = intent.getBooleanExtra("fromRecent", false)
      //  Toast.makeText(this, "$fromRecentUsersActivity", Toast.LENGTH_SHORT).show()

        if (!fromRecentUsersActivity && email != null && email != "null" && password != null && password != "null") {
            val myLiveData = newsFeedActivityViewModel.getMe(auth.currentUser?.uid.toString())
            myLiveData?.observe(this, { user ->
                val userName = user.name
                val userImageUrl = user.profileImageUrl
                getUserToken().addOnSuccessListener {
                    val token = it.token
                    val currentUser = RecentLoggedInUser(email, password, userName, userImageUrl)

                    addUserToRecentUsers(currentUser, token).addOnCompleteListener {
                        if (!it.isSuccessful) {
                            newsFeedActivityViewModel.createRecentUsersCollection(token,currentUser).addOnCompleteListener {
                                addUserToRecentUsers(currentUser, token)
                            }
                        }
                    }

                }

            })
        }
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

    private fun addUserToRecentUsers(user: RecentLoggedInUser, token: String): Task<Void>{
        return newsFeedActivityViewModel.addUserToRecentLoggedInUsers(user, token)
    }

    private fun getUserToken(): Task<InstanceIdResult> {
        return FirebaseInstanceId.getInstance().instanceId
    }
}