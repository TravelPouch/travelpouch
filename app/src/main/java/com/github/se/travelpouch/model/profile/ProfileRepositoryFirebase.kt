package com.github.se.travelpouch.model.profile

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
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
  private var documentReference: DocumentReference? = null

  override suspend fun initAfterLogin(onSuccess: (Profile) -> Unit) {
    val user = Firebase.auth.currentUser

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
        addProfile(user.email!!, user.uid, onSuccess)
      } catch (e: Exception) {
        Log.e("nullEmail", "Email of user was null, deleting user")
      }
    } else {
      documentReference = document.reference
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
    try {
      val document = db.collection(collectionPath).document(documentPath).get().await()
      addingUserIfNotRegistered(user, document, onSuccess)
    } catch (e: Exception) {
      Log.e("GetProfileCollectionFailed", "Failed to fetch the user collection")
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
            username = email.substringBefore("@"),
            email = email,
            friends = emptyList(),
            name = email.substringBefore("@"),
            emptyList())

    documentReference = db.collection(collectionPath).document(uid)
    performFirestoreOperation(
        documentReference!!.set(profile),
        onSuccess = {
          Log.d("ProfileCreated", "profile created")
          onSuccess(profile)
        },
        onFailure = {
          Log.e("ErrorProfile", "Error while creating profile")
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
          Log.e("EventRepository", "Error getting documents", e)
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

  override fun addFriend(
      email: String,
      userProfile: Profile,
      updatingFunction: (Profile, String) -> Profile,
      onSuccess: (Profile) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    var friendsDocumentReference: DocumentReference? = null
    db.collection(collectionPath)
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener {
          if (it.isEmpty) {
            onFailure(Exception("user not found"))
          } else {
            val document = it.documents[0]
            friendsDocumentReference = document.reference
            val friendProfile = ProfileRepositoryConvert.documentToProfile(document)
            if (friendProfile == ErrorProfile.errorProfile) {
              onFailure(Exception("user corrupted"))
            } else {
              val userProfileUpdated = updatingFunction(userProfile, friendProfile.email)
              db.runTransaction { t ->
                    t.update(documentReference!!, "friends", userProfileUpdated.friends)
                    t.update(
                        friendsDocumentReference!!,
                        "friends",
                        updatingFunction(friendProfile, userProfile.email).friends)
                  }
                  .addOnSuccessListener { onSuccess(userProfileUpdated) }
                  .addOnFailureListener { onFailure(Exception("failed to add user as friend")) }
            }
          }
        }
        .addOnFailureListener { onFailure(Exception("getting friend profile failed")) }
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
        val friends = document.get("friends") as? List<String>
        val userTravelList = document.get("listoftravellinked") as? List<String>
        val name = document.getString("name")

        Profile(
            fsUid = uid,
            username = username!!,
            email = email!!,
            friends = friends ?: emptyList(),
            name = name!!,
            userTravelList = userTravelList ?: emptyList())
      } catch (e: Exception) {
        Log.e("ProfileRepository", "Error converting document to Profile", e)
        ErrorProfile.errorProfile
      }
    }
  }
}
