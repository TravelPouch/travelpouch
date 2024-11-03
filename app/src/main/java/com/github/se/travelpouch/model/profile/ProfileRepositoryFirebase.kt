package com.github.se.travelpouch.model.profile

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * The class representing the communication scheme between our project and the database that will
 * allow us to store and retrieve profiles.
 *
 * @param db (FirebaseFirestore) : the database used to store our profiles
 */
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

  /**
   * This function adds a user if its profile does not exist. If an error occurs, we delete the user
   * from the database.
   *
   * @param user (FirebaseUser) : the FirebaseUser that is connected on the application
   * @param document (DocumentSnapshot) : the document containing the profile of the connected user.
   */
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

  /**
   * This function gets a user profile. If the profile does not exist we create a profile for the
   * user.
   *
   * @param user (FirebaseUser) : the currently connected user on the application
   * @param onSuccess (() -> Unit) : the function to apply after having a valid profile
   */
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

  /**
   * This function adds a profile to Firebase
   *
   * @param email (String) : the email of the user
   * @param uid (String) : the unique identifier of the user
   */
  private fun addProfile(email: String, uid: String) {
    val profile =
        Profile(
            fsUid = uid,
            username = email.substringBefore("@") + uid,
            email = email,
            friends = null,
            name = email.substringBefore("@"),
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

  /**
   * This function is a helper function that safely performs a Firebase operation. A task has
   * listeners added to it. If the task is successful, we apply onSuccess. Otherwise we perform
   * onFailure.
   *
   * @param task (Task<Void>) : a task to perform
   * @param onSuccess (() -> Unit) : the function called when the profile is correctly added/updated
   *   to the database
   * @param onFailure ((Exception) -> Unit) : the function called when an error occurs during the
   *   adding/updating a profile to the database
   */
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

/** This class is used to convert a document to a profile, across the project */
class ProfileRepositoryConvert {
  companion object {
    /**
     * This function converts a document got from Firebase to a profile. It returns an error profile
     * if an error occurs
     *
     * @param document (DocumentSnapshot) : The document from Firebase
     * @return (Profile) : If the conversion goes without a problem a profile is return. Otherwise,
     *   the error profile is returned.
     */
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
