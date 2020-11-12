package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.react.React


interface ReactClickListener {
    fun onReactButtonLongClicked()
    fun onReactButtonClicked() //for adding
    fun onReactButtonClicked(react: React?) //for deleting
//    fun onReactsClicked()
}