package com.example.facebook_clone.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.NewsFeedTabsAdapter
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.viewmodel.activity.NewsFeedActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_news_feed.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "NewsFeedActivity"
private const val TOKEN = "token"
class NewsFeedActivity : AppCompatActivity() {
    private val newsFeedActivityViewModel by viewModel<NewsFeedActivityViewModel>()
    private val postViewModel by viewModel<PostViewModel>()
    private var fromRecentUsersActivity = false
    private val auth: FirebaseAuth by inject()
    private lateinit var currentUser: User
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
        val myLiveData = newsFeedActivityViewModel.getMe(auth.currentUser?.uid.toString())
        myLiveData?.observe(this, { user ->
            currentUser = user
            getUserToken().addOnSuccessListener {
                val newToken = it.token
                val tokenFromSharedPref = getTokenFromSharedPreference(this)
                if (newToken != tokenFromSharedPref) {
                    saveTokenToSharedPreference(this, newToken)
//                    updateTokenInCurrentUserPosts(user.id.toString(), "newToken")
//                    val userGroups = user.groups.orEmpty()
//                    if (userGroups.isNotEmpty()){
//                        for (group in userGroups){
//                            updateTokenInUserGroupPosts(user.id.orEmpty(), group.id.orEmpty(), "newToken")
//                        }
//                    }

                }
                updateUserToken(newToken, currentUser.id.toString())
            }
            if (!fromRecentUsersActivity && email != null && email != "null" && password != null && password != "null") {

                val userName = currentUser.name
                val userImageUrl = currentUser.profileImageUrl
                getUserToken().addOnSuccessListener {
                    val token = it.token
                    val currentUser = RecentLoggedInUser(email, password, userName, userImageUrl)

                    addUserToRecentUsers(currentUser, token).addOnCompleteListener {
                        if (!it.isSuccessful) {
                            newsFeedActivityViewModel.createRecentUsersCollection(
                                token,
                                currentUser
                            ).addOnCompleteListener {
                                addUserToRecentUsers(currentUser, token)
                            }
                        }
                    }

                }
            }

        })

        searchIconImageView.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateUserToken(token: String, userId: String) {
        newsFeedActivityViewModel.updateUserToken(token, userId)
    }

    private fun setUpTabs() {
       val tabsAdapter = NewsFeedTabsAdapter(supportFragmentManager)
        if (viewPager != null){
            viewPager.adapter = tabsAdapter
            newsFeedTabLayout.setupWithViewPager(viewPager)
        }


        newsFeedTabLayout.getTabAt(0)?.setIcon(R.drawable.ic_news_feed)
        newsFeedTabLayout.getTabAt(1)?.setIcon(R.drawable.ic_group_members)
        newsFeedTabLayout.getTabAt(2)?.setIcon(R.drawable.ic_alarm)
        newsFeedTabLayout.getTabAt(3)?.setIcon(R.drawable.ic_menu)


//        newsFeedTabLayout.setOnTabSelectedListener(
//            object : ViewPagerOnTabSelectedListener(viewPager) {
//                override fun onTabSelected(tab: TabLayout.Tab) {
//                    super.onTabSelected(tab)
//                    val tabIconColor = ContextCompat.getColor(this@NewsFeedActivity, R.color.medium_blue)
//                    when(tab.position){
//                        0 -> {
//                            tab.icon = R.drawable.ic_news_feed_filled
//                        }
//                        1 -> {
//
//                        }
//                        2 -> {
//
//                        }
//                        3 -> {
//
//                        }
//                    }
//
//                }
//
//                override fun onTabUnselected(tab: TabLayout.Tab) {
//                    super.onTabUnselected(tab)
//                    val tabIconColor =
//                        ContextCompat.getColor(this@NewsFeedActivity, R.color.black)
//                    tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
//                }
//
//                override fun onTabReselected(tab: TabLayout.Tab) {
//                    super.onTabReselected(tab)
//                }
//            }
//        )

    }

    private fun addUserToRecentUsers(user: RecentLoggedInUser, token: String): Task<Void>{
        return newsFeedActivityViewModel.addUserToRecentLoggedInUsers(user, token)
    }

    private fun getUserToken(): Task<InstanceIdResult> {
        return FirebaseInstanceId.getInstance().instanceId
    }

    private fun saveTokenToSharedPreference(context: Context?, token: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(TOKEN, token)
            .apply()
    }

    companion object {
        fun getTokenFromSharedPreference(context: Context?): String? {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(TOKEN, "")
        }
    }
//    private fun updateTokenInCurrentUserPosts(userId: String, token: String){
//        postViewModel.updateTokenInProfilePost(userId, token)
//    }

//    private fun updateTokenInUserGroupPosts(userId: String, groupId: String, token: String){
//        postViewModel.updateTokenInGroupPost(userId, groupId, token)
//    }

//    private fun updateTokenInCurrentUserComments(userId: String, token: String){
//        postViewModel.updateTokenInComment(userId, token)
//    }


}