package com.example.facebook_clone.model.post.comment

import com.example.facebook_clone.model.post.react.React

data class ReactionsAndSubComments (var reactions: List<React>? = null, var subComments: List<Comment>? = null)