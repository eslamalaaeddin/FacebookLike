package com.example.facebook_clone.livedata

import androidx.lifecycle.LiveData
import com.example.facebook_clone.model.post.Post
import com.google.firebase.firestore.*

class PostLiveData (private val documentReference: DocumentReference) : LiveData<Post>(),
    EventListener<DocumentSnapshot> {

    private var listenerRegistration : ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        listenerRegistration = documentReference.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration?.remove()
    }

    override fun onEvent(snapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        val post = snapshot?.toObject(Post::class.java)
        postValue(post)
    }



}