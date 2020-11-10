package com.example.facebook_clone.livedata

import androidx.lifecycle.LiveData
import com.example.facebook_clone.model.User
import com.google.firebase.firestore.*

class UserLiveData (private val documentReference: DocumentReference) : LiveData<User>(),
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
        val user = snapshot?.toObject(User::class.java)
        postValue(user)
    }



}