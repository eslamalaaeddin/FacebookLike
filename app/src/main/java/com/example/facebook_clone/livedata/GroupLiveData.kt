package com.example.facebook_clone.livedata

import androidx.lifecycle.LiveData
import com.example.facebook_clone.model.group.Group
import com.example.facebook_clone.model.user.User
import com.google.firebase.firestore.*

class GroupLiveData (private val documentReference: DocumentReference) : LiveData<Group>(),
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

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        val group = value?.toObject(Group::class.java)
        postValue(group)
    }

}