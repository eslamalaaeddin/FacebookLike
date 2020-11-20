package com.example.facebook_clone.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.LiveData
import com.example.facebook_clone.R
import com.example.facebook_clone.adapter.RecentSearchesAdapter
import com.example.facebook_clone.adapter.SearchedUsersAdapter
import com.example.facebook_clone.helper.listener.SearchedItemListener
import com.example.facebook_clone.model.user.User
import com.example.facebook_clone.viewmodel.SearchActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_search.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "SearchActivity"

class SearchActivity : AppCompatActivity(), SearchedItemListener {
    private val auth: FirebaseAuth by inject()
    private var usersSearchResultLiveData: LiveData<List<User>>? = null
    private val searchActivityViewModel by viewModel<SearchActivityViewModel>()
    private lateinit var searchedUsersAdapter: SearchedUsersAdapter
    //private lateinit var recentSearchesAdapter: RecentSearchesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        upButtonImageView.setOnClickListener { finish() }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(query: Editable?) {
                if (query?.isNotEmpty()!!) {
                    recentSearchRecyclerView.visibility = View.GONE
                    if (query.toList().size >= 3) {
                        usersSearchResultRecyclerView.visibility = View.VISIBLE
//                        pagesSearchResultRecyclerView.visibility = View.VISIBLE
//                        groupsSearchResultRecyclerView.visibility = View.VISIBLE
                        usersSearchResultLiveData =
                            searchActivityViewModel.searchForUsers(query.toString())
                        if (usersSearchResultLiveData != null) {
                            usersSearchResultLiveData?.observe(this@SearchActivity, { users ->
                                searchedUsersAdapter =
                                    SearchedUsersAdapter(users, this@SearchActivity)
                                usersSearchResultRecyclerView.adapter = searchedUsersAdapter
                            })
                        }
                    }
                } else {
                    recentSearchRecyclerView.visibility = View.VISIBLE
                    //recentSearchesAdapter = RecentSearchesAdapter()
                   // recentSearchRecyclerView.adapter = recentSearchesAdapter
                    usersSearchResultRecyclerView.visibility = View.GONE
                    pagesSearchResultRecyclerView.visibility = View.GONE
                    groupsSearchResultRecyclerView.visibility = View.GONE
                }
            }
        })
    }

    override fun onSearchedUserClicked(userId: String) {
        if (userId == auth.currentUser?.uid.toString()){
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        else {
            val intent = Intent(this, OthersProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    override fun onSearchedPageClicked(pageId: String) {
    }

    override fun onSearchedGroupClicked(groupId: String) {
    }

    override fun onBackPressed() {
        finish()
    }

}