package com.example.facebook_clone.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.facebook_clone.ui.fragment.topdestinations.GroupsFragment
import com.example.facebook_clone.ui.fragment.topdestinations.MenuFragment
import com.example.facebook_clone.ui.fragment.topdestinations.NewsFeedFragment
import com.example.facebook_clone.ui.fragment.topdestinations.NotificationsFragment


class NewsFeedTabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {



    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> return NewsFeedFragment()
            1 -> return GroupsFragment()
            2 -> return NotificationsFragment()
            else -> return MenuFragment()
        }

    }

    override fun getCount(): Int = 4

    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0 -> return ""
            1 -> return ""
            2 -> return ""
            else -> return ""
        }
    }
}