package com.github.se.travelpouch.model.activity

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * This class represents the view model of the activities
 *
 * @property activityRepositoryFirebase (ActivityRepository) : the repository of the activites
 */
class ActivityViewModel(val activityRepositoryFirebase: ActivityRepository) : ViewModel() {

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityViewModel(ActivityRepositoryFirebase(Firebase.firestore)) as T
          }
        }
  }

  private val activities_ = MutableStateFlow<List<Activity>>(emptyList())
  val activities: StateFlow<List<Activity>> = activities_.asStateFlow()

  private val selectedActivity_ = MutableStateFlow<Activity?>(null)
  val selectedActivity: StateFlow<Activity?> = selectedActivity_.asStateFlow()

  private val onFailureTag = "ActivityViewModel"

  /** This is the initialisation function of the model view */
  init {
    activityRepositoryFirebase.init { getAllActivities() }
  }

  /** This function gets all the activities from the database */
  fun getAllActivities() {
    activityRepositoryFirebase.getAllActivities(
        onSuccess = { activities_.value = it },
        onFailure = { Log.e(onFailureTag, "Failed to get all activities", it) })
  }

  /**
   * This function adds an activity to the database
   *
   * @param activity (Activity) : the activity to add to the database
   */
  fun addActivity(activity: Activity, context: Context) {
    activityRepositoryFirebase.addActivity(
        activity,
        onSuccess = {
          getAllActivities()
          Toast.makeText(context, "Activity saved", Toast.LENGTH_SHORT).show()
        },
        onFailure = {
          Log.e(onFailureTag, "Failed to add an activity", it)
          Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
        })
  }

  /**
   * This function updates an activity in the database
   *
   * @param activity (Activity) : the activity to update in the database
   */
  fun updateActivity(activity: Activity, context: Context) {
    activityRepositoryFirebase.updateActivity(
        activity,
        onSuccess = {
          getAllActivities()
          Toast.makeText(context, "Activity updated", Toast.LENGTH_SHORT).show()
        },
        onFailure = {
          Log.e(onFailureTag, "Failed to update an activity", it)
          Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
        })
  }

  /**
   * This function deletes an activity from the database
   *
   * @param activity (Activity) : the activity to delete from the database
   */
  fun deleteActivityById(activity: Activity, context: Context) {
    activityRepositoryFirebase.deleteActivityById(
        activity.uid,
        onSuccess = {
          getAllActivities()
          Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
        },
        onFailure = {
          Log.e(onFailureTag, "Failed to delete an activity", it)
          Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
        })
  }

  /**
   * This function gives us an unused uniques identifier
   *
   * @return (String) : an unused uniques identifier
   */
  fun getNewUid(): String {
    return activityRepositoryFirebase.getNewUid()
  }

  /**
   * This function selects an activity.
   *
   * @param activity (Activity): the activity that was selected
   */
  fun selectActivity(activity: Activity) {
    selectedActivity_.value = activity
  }

  /** This function sorts the activities by date */
  fun getSortedActivities(): List<Activity> {
    return activities_.value.sortedBy { it.date }
  }
}
