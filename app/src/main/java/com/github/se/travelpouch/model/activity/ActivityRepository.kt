package com.github.se.travelpouch.model.activity

import com.google.firebase.firestore.DocumentReference

interface ActivityRepository {
  /**
   * This function allows us to retrieve all the activities from Firebase.
   *
   * @param onSuccess ((List<Activity>) -> Unit) : the function to apply when the retrieving goes
   *   without any problem
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs during the
   *   fetching of the activities from the database
   */
  fun getAllActivities(onSuccess: (List<Activity>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * This function gives us an unused unique identifier.
   *
   * @return (String) : an unused unique identifier
   */
  fun getNewUid(): String

  /**
   * This function adds a new activity to the Firebase database.
   *
   * @param activity (Activity) : the activity to add in Firebase
   * @param onSuccess (() -> Unit) : the function to call when we successfully add an activity to
   *   the database
   * @param onFailure ((Exception) -> Unit) : the function to call when an error occurs during the
   *   adding of an activity to the database
   * @param eventDocumentReference (DocumentReference) : The newly created event document reference
   *   to allow completion of the event at the creation of an activity
   */
  fun addActivity(
      activity: Activity,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      eventDocumentReference: DocumentReference
  )

  /**
   * The initialisation function of the interface
   *
   * @param onSuccess (() -> Unit) : the function to call when the initialisation is successful
   */
  fun setIdTravel(onSuccess: () -> Unit, travelId: String)

  /**
   * This function updates an activity already present in the database.
   *
   * @param activity (Activity) : the activity to update in Firebase
   * @param onSuccess (() -> Unit) : the function to apply when the activity is successfully updated
   * @param onFailure ((Exception) -> Unit) : the function to apply when an error occurs during the
   *   update of an activity
   */
  fun updateActivity(activity: Activity, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

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
  fun deleteActivityById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
