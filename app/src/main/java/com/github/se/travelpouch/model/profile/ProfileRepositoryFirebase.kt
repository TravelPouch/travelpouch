package com.github.se.travelpouch.model.profile

import android.util.Log
import android.widget.Toast
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.events.Event
import com.google.android.gms.tasks.Task
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class ProfileRepositoryFirebase(private val db: FirebaseFirestore): ProfileRepository {

    private var collectionPath = ""

    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    override fun init(onSuccess: () -> Unit) {
        Firebase.auth.addAuthStateListener {
            val user = it.currentUser
            var flag = false
            if (user != null) {
                db.collection(user.uid).get().addOnSuccessListener {
                    result ->
                        if(result.isEmpty){
                            flag = true
                            try {
                                addProfile(user.email!!, user.uid)
                            }catch(e: Exception){
                                Log.e("nullEmail", "Email of user was null")
                                user.delete()
                                Firebase.auth.signOut()
                            }
                        }
                    collectionPath = user.uid
                    onSuccess()
                }.addOnFailureListener {
                    Log.e("GetProfileCollectionFailed", "Failed to fetch the user collection")
                }
            }

            if(!flag){
                Log.d("problem", "wtf, listener useless")
            }
        }
    }

    private fun addProfile(email: String, uid: String){
        val profile = Profile(
            uid = uid,
            username = email.substringBefore("@") + uid,
            email = email,
            friends = null
        )
        performFirestoreOperation(
            db.collection(uid).document("profile").set(profile),
            onSuccess = {
                Log.d("ProfileCreated", "profile created")
            },
            onFailure = {
                Log.e("ErrorProfile", "Error while creating profile")
                //has to correct thing
            })
    }

    override fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("ProfileRepository", "getProfile")
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { result ->
                val profile = result?.mapNotNull { documentToProfile(it) }?.get(0) ?: ErrorProfile.errorProfile
                onSuccess(profile)
            }
            .addOnFailureListener { e ->
                Log.e("EventRepository", "Error getting documents", e)
                onFailure(e)
            }
    }

    override fun updateProfile(
        newProfile: Profile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("ProfileRepositoryFirestore", "updateProfile")
        performFirestoreOperation(
            db.collection(collectionPath).document(newProfile.uid).set(newProfile), onSuccess, onFailure)
    }

    private fun performFirestoreOperation(
        task: Task<Void>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        task
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e("EventRepositoryFirestore", "Error performing Firestore operation", e)
                onFailure(e)
            }
    }

    private fun documentToProfile(document: DocumentSnapshot): Profile {
        return try {
            val uid = document.id
            val username = document.getString("username")
            val email = document.getString("email")
            val friendsData = document["documentsNeeded"] as? Map<*, *>
            val friends =
                friendsData?.map { (key, value) -> key as Int to value as String }?.toMap()

            Profile(
                uid = uid,
                username = username!!,
                email = email!!,
                friends
            )
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error converting document to Profile", e)
            ErrorProfile.errorProfile
        }
    }
}