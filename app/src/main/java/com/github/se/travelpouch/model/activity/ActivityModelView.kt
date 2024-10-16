package com.github.se.travelpouch.model.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.travelpouch.model.events.EventRepositoryFirebase
import com.github.se.travelpouch.model.events.EventViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActivityModelView(val activityRepositoryFirebase: ActivityRepositoryFirebase) : ViewModel() {

  private val activities_ = MutableStateFlow<List<Activity>>(emptyList())
  val activities: StateFlow<List<Activity>> = activities_.asStateFlow()

  fun init() {
    activityRepositoryFirebase.init { getActivities() }
  }

  fun getActivities() {
    activityRepositoryFirebase.getActivity(onSuccess = { activities_.value = it }, onFailure = {})
  }

  fun addActivity(activity: Activity) {
    activityRepositoryFirebase.addActivity(activity, { getActivities() }, {})
  }

  fun updateActivity(activity: Activity) {
    activityRepositoryFirebase.updateActivity(activity, { getActivities() }, {})
  }

  fun deleteActivityById(activity: Activity) {
    activityRepositoryFirebase.deleteActivityById(activity.uid, { getActivities() }, {})
  }

  fun getNewUid(): String {
    return activityRepositoryFirebase.getNewUid()
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventViewModel(EventRepositoryFirebase(Firebase.firestore)) as T
          }
        }
  }
}
