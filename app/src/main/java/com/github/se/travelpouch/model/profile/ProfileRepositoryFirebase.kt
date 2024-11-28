package com.github.se.travelpouch.model.profile

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * The class representing the communication scheme between our project and the database that will
 * allow us to store and retrieve profiles.
 *
 * @param db (FirebaseFirestore) : the database used to store our profiles
 */
class ProfileRepositoryFirebase(private val db: FirebaseFirestore) : ProfileRepository {

  private var collectionPath = "userslist"
  private var documentPath = ""

  override suspend fun initAfterLogin(onSuccess: (Profile) -> Unit) {
    val user = Firebase.auth.currentUser
    Log.d("ProfileRepository", "initAfterLogin: ${user?.uid} ${user?.email}")

    if (user != null) {
      documentPath = user.uid
      gettingUserProfile(user, onSuccess)
    } else {
      Firebase.auth.signOut()
    }
  }

  /**
   * This function adds a user if its profile does not exist. If an error occurs, we delete the user
   * from the database.
   *
   * @param user (FirebaseUser) : the FirebaseUser that is connected on the application
   * @param document (DocumentSnapshot) : the document containing the profile of the connected user.
   */
  private fun addingUserIfNotRegistered(
      user: FirebaseUser,
      document: DocumentSnapshot,
      onSuccess: (Profile) -> Unit
  ) {
    if (!document.exists()) {
      try {
        Log.d("ProfileRepository", "Creating profile for user ${user.uid} ${user.email}")
        addProfile(user.email!!, user.uid, onSuccess)
      } catch (e: Exception) {
        Log.e("ProfileRepository", "Email of user was null, deleting user")
      }
    } else {
      Log.d("ProfileRepository", "addingUserIfNotRegistered: User profile exists")
      onSuccess(ProfileRepositoryConvert.documentToProfile(document))
    }
  }

  /**
   * This function gets a user profile. If the profile does not exist we create a profile for the
   * user.
   *
   * @param user (FirebaseUser) : the currently connected user on the application
   * @param onSuccess (() -> Unit) : the function to apply after having a valid profile
   */
  suspend fun gettingUserProfile(user: FirebaseUser, onSuccess: (Profile) -> Unit) {
    Log.d("ProfileRepository", "Getting user profile")
    try {
      val document = db.collection(collectionPath).document(documentPath).get().await()
      Log.d("ProfileRepository", "Got document")
      addingUserIfNotRegistered(user, document, onSuccess)
    } catch (e: Exception) {
      Log.e("ProfileRepository", "Failed to fetch the user collection", e)
    }
  }

  /**
   * Retrieves the Firestore UID associated with the given email.
   *
   * @param email The email address to search for.
   * @param onSuccess A callback function that is invoked with the Firestore UID if found, or null
   *   if not found.
   * @param onFailure A callback function that is invoked with an Exception if an error occurs
   *   during the operation.
   */
  @SuppressLint("SuspiciousIndentation")
  override fun getFsUidByEmail(
      email: String,
      onSuccess: (String?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { result ->
          if (result.documents.isNotEmpty()) {
            val fsUid = result.documents[0].id
            Log.d("ProfileRepository", "fsUid: $fsUid")
            onSuccess(fsUid)
          } else {
            Log.d("ProfileRepository", "fsUid: null")
            onSuccess(null)
          }
        }
        .addOnFailureListener { e ->
          Log.e("ProfileRepository", "Error getting fsUid by email", e)
          onFailure(e)
        }
  }

  /**
   * This function adds a profile to Firebase
   *
   * @param email (String) : the email of the user
   * @param uid (String) : the unique identifier of the user
   */
  private fun addProfile(email: String, uid: String, onSuccess: (Profile) -> Unit) {
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
        onSuccess = {
          Log.d("ProfileRepository", "profile created")
          onSuccess(profile)
        },
        onFailure = {
          Log.e("ProfileRepository", "Error while creating profile")
          // has to correct thing
        })
  }

  /**
   * Retrieves the profile elements from the Firestore database.
   *
   * @param onSuccess A callback function that is invoked with the retrieved Profile object.
   * @param onFailure A callback function that is invoked with an Exception if an error occurs
   *   during the operation.
   */
  override fun getProfileElements(onSuccess: (Profile) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(documentPath)
        .get()
        .addOnSuccessListener { result ->
          val profile = ProfileRepositoryConvert.documentToProfile(result)
          onSuccess(profile)
        }
        .addOnFailureListener { e ->
          Log.e("ProfileRepository", "Error getting documents", e)
          onFailure(e)
        }
  }

  /**
   * Updates the profile in the Firestore database.
   *
   * @param newProfile The new profile data to be updated.
   * @param onSuccess A callback function that is invoked when the profile is successfully updated.
   * @param onFailure A callback function that is invoked with an Exception if an error occurs
   *   during the update operation.
   */
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

        Log.d("ProfileRepository", "Document to Profile: $uid, $username, $email, $friends, $name, $userTravelList")

        Profile(
            fsUid = uid,
            username = username!!,
            email = email!!,
            friends = friends,
            name = name!!,
            userTravelList = userTravelList ?: emptyList())
      } catch (e: Exception) {
        Log.e("ProfileRepository", "Error converting document to Profile", e)
        ErrorProfile.errorProfile
      }
    }
  }
}
