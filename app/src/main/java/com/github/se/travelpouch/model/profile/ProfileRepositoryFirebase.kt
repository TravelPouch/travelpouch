package com.github.se.travelpouch.model.profile

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepositoryFirebase(private val db: FirebaseFirestore) : ProfileRepository {

  private var collectionPath = "userslist"
  private var documentPath = ""

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      val user = it.currentUser
      if (user != null) {
        gettingUserProfile(user, onSuccess)
      }
    }
  }

  private fun addingUserIfNotRegistered(user: FirebaseUser, document: DocumentSnapshot) {
    if (!document.exists()) {
      try {
        addProfile(user.email!!, user.uid)
      } catch (e: Exception) {
        Log.e("nullEmail", "Email of user was null")
        user.delete()
        Firebase.auth.signOut()
      }
    }
  }

  fun gettingUserProfile(user: FirebaseUser, onSuccess: () -> Unit) {

    documentPath = user.uid
    db.collection(collectionPath)
        .document(documentPath)
        .get()
        .addOnSuccessListener { result ->
          addingUserIfNotRegistered(user, result)
          onSuccess()
        }
        .addOnFailureListener {
          Log.e("GetProfileCollectionFailed", "Failed to fetch the user collection")
        }
  }

  private fun addProfile(email: String, uid: String) {
    val profile =
        Profile(
            fsUid = uid,
            username = email.substringBefore("@") + uid,
            email = email,
            friends = null,
            name = "",
            emptyList())
    performFirestoreOperation(
        db.collection(collectionPath).document(uid).set(profile),
        onSuccess = { Log.d("ProfileCreated", "profile created") },
        onFailure = {
          Log.e("ErrorProfile", "Error while creating profile")
          // has to correct thing
        })
  }

  override fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit) {
    Log.d("ProfileRepository", "getProfile")
    db.collection(collectionPath)
        .document(documentPath)
        .get()
        .addOnSuccessListener { result ->
          val profile = ProfileRepositoryConvert.documentToProfile(result)
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
        db.collection(collectionPath).document(documentPath).set(newProfile), onSuccess, onFailure)
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
}

class ProfileRepositoryConvert {
  companion object {
    fun documentToProfile(document: DocumentSnapshot): Profile {
      return try {
        val uid = document.id
        val username = document.getString("username")
        val email = document.getString("email")
        val friendsData = document["documentsNeeded"] as? Map<*, *>
        val friends = friendsData?.map { (key, value) -> key as Int to value as String }?.toMap()
        val userTravelList = document.get("listoftravellinked") as? List<String>
        val name = document.getString("name")

        Profile(
            fsUid = uid,
            username = username!!,
            email = email!!,
            friends = friends,
            name!!,
            userTravelList = userTravelList ?: emptyList())
      } catch (e: Exception) {
        Log.e("ProfileRepository", "Error converting document to Profile", e)
        ErrorProfile.errorProfile
      }
    }
  }
}
