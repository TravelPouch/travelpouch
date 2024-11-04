package com.github.se.travelpouch.model.activity

import android.util.Log
import com.github.se.travelpouch.model.FirebasePaths
import com.github.se.travelpouch.model.travels.Location
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This class represents the repository that communicates with Firebase to store, retrieve, delete
 * and update activities.
 */
class ActivityRepositoryFirebase(private val db: FirebaseFirestore) : ActivityRepository {

  private var collectionPath = ""

  /**
   * This function gives us an unused unique identifier.
   *
   * @return (String) : an unused unique identifier
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * The initialisation function of the repository
   *
   * @param onSuccess (() -> Unit) : the function to call when the initialisation is successful
   */
  override fun initAfterTravelAccess(onSuccess: () -> Unit, travelId: String) {
    val p1 = FirebasePaths.TravelsSuperCollection
    val p2 = FirebasePaths.activities
    collectionPath = FirebasePaths.constructPath(p1, travelId, p2)
    onSuccess()
  }

  /**
   * This function updates an activity already present in the database.
   *
   * @param activity (Activity) : the activity to update in Firebase
   * @param onSuccess (() -> Unit) : the function to apply when the activity is successfully updated
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs during the
   *   update of an activity
   */
  override fun updateActivity(
      activity: Activity,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("ActivityRepositoryFirestore", "updateActivity")
    performFirestoreOperation(
        db.collection(collectionPath).document(activity.uid).set(activity), onSuccess, onFailure)
  }

  /**
   * This function deletes an activity from the database based on its identifier.
   *
   * @param id (String) : the identifier on which we base ourselves to delete an activity from the
   *   firebase
   * @param onSuccess (() -> Unit) : the function to call when the deletion of the activity is
   *   successful
   * @param onFailure ((Exception) -> Unit) : the function to call when an error occurs during the
   *   deletion of an activity
   */
  override fun deleteActivityById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("ActivityRepositoryFirestore", "deleteTravelById")
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  /**
   * This function allows us to retrieve all the activities from Firebase.
   *
   * @param onSuccess ((List<Activity>) -> Unit) : the function to apply when the retrieving goes
   *   without any problem
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs during the
   *   fetching of the activities from the database
   */
  override fun getAllActivities(
      onSuccess: (List<Activity>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("ActivityRepository", "getEvents")
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { result ->
          val activities = result?.mapNotNull { documentToActivity(it) } ?: emptyList()
          onSuccess(activities)
        }
        .addOnFailureListener { e ->
          Log.e("ActivityRepository", "Error getting documents", e)
          onFailure(e)
        }
  }

  /**
   * This function adds a new activity to the Firebase database.
   *
   * @param activity (Activity) : the activity to add in Firebase
   * @param onSuccess (() -> Unit) : the function to call when we successfully add an activity to
   *   the database
   * @param onFailure ((Exception) -> Unit) : the function to call when an error occurs during the
   *   adding of an activity to the database
   */
  override fun addActivity(
      activity: Activity,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(activity.uid).set(activity), onSuccess, onFailure)
  }

  /**
   * This function is a helper function that safely performs a Firebase operation. A task has
   * listeners added to it. If the task is successful, we apply onSuccess. Otherwise we perform
   * onFailure.
   *
   * @param task (Task<Void>) : a task to perform
   * @param onSuccess (() -> Unit) : the function called when the event is correctly added to the
   *   database
   * @param onFailure ((Exception) -> Unit) : the function called when an error occurs during the
   *   adding an event to the database
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
          Log.e("ActivityRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
  }

  /**
   * This function converts a document got from Firebase to an activity. It returns null if an error
   * occurs.
   *
   * @param document (DocumentSnapshot) : The document from Firebase
   * @return (Activity?) : If the conversion goes without a problem an event is return. Otherwise,
   *   null is returned.
   */
  private fun documentToActivity(document: DocumentSnapshot): Activity? {
    return try {
      val uid = document.id
      val title = document.getString("title")
      val description = document.getString("description")
      val date = document.getTimestamp("date")
      val documentsNeededData = document["documentsNeeded"] as? Map<*, *>
      val documentsNeeded =
          documentsNeededData?.map { (key, value) -> key as String to value as Int }?.toMap()
      val locationData = document.get("location") as? Map<*, *>
      val location =
          locationData?.let {
            Location(
                latitude = it["latitude"] as? Double ?: 0.0,
                longitude = it["longitude"] as? Double ?: 0.0,
                insertTime = it["insertTime"] as? Timestamp ?: Timestamp(0, 0),
                name = it["name"] as? String ?: "")
          }

      Activity(
          uid = uid,
          title = title!!,
          description = description!!,
          date = date!!,
          documentsNeeded = documentsNeeded,
          location = location!!)
    } catch (e: Exception) {
      Log.e("ActivityRepository", "Error converting document to Activity", e)
      null
    }
  }
}
