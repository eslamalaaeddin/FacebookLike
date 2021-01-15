package com.example.facebook_clone.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.RecentLoggedInUsersAdapter
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.RecentUsersClickListener
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUsersDocument
import com.example.facebook_clone.viewmodel.fragment.LoginFragmentViewModel
import com.example.facebook_clone.viewmodel.activity.RecentUsersActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_recent_users.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "RecentUsersActivity"
class RecentUsersActivity : AppCompatActivity(), RecentUsersClickListener {
    private val recentUsersActivityViewModel by viewModel<RecentUsersActivityViewModel>()
    private val loginFragmentViewModel by viewModel<LoginFragmentViewModel>()
    private lateinit var recentLoggedInUsersAdapter: RecentLoggedInUsersAdapter
    private var list = mutableListOf<RecentLoggedInUser>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_users)

        getUserToken().addOnSuccessListener {
            recentUsersActivityViewModel.getRecentLoggedInUsers(it.token).addSnapshotListener { value, error ->
                val recentUsers = value?.toObject(RecentLoggedInUsersDocument::class.java)?.recentUsers
                recentLoggedInUsersAdapter = RecentLoggedInUsersAdapter(recentUsers.orEmpty(), this)
                recentUsersRecyclerView.adapter = recentLoggedInUsersAdapter
            }
        }
        createNewFacebookAccount.setOnClickListener {navigateToUserNameFragment()}
        logIntoAnotherAccount.setOnClickListener { navigateToLoginFragment() }

    }
    //false ==> new user
    //true ==> existing user
    private fun navigateToUserNameFragment(){
        RegisteringActivity.open(this, false)
        finish()
    }

    private fun navigateToLoginFragment(){
        RegisteringActivity.open(this, true)
        finish()
    }

    private fun getUserToken(): Task<InstanceIdResult> {
        return FirebaseInstanceId.getInstance().instanceId
    }

    override fun onRecentUserClicked(email: String, password: String) {
        signInAndNavigate(email, password)
    }

    private fun signInAndNavigate(mail: String, password: String){
        loginFragmentViewModel.signIn(mail, password).addOnCompleteListener {task ->
            if (task.isSuccessful){
                navigateToNewsFeedActivity()
            }else{
                Utils.toastMessage(this, task.exception?.message.toString())
            }
        }
    }
    private fun navigateToNewsFeedActivity(){
        val intent = Intent(this, NewsFeedActivity::class.java)
        intent.putExtra("fromRecent", true)
        startActivity(intent)
        finish()
    }
}