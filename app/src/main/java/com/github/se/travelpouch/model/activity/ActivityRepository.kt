package com.github.se.travelpouch.model.activity

interface ActivityRepository {

  fun getActivity(onSuccess: (List<Activity>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNewUid(): String

  fun addActivity(activity: Activity, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun init(onSuccess: () -> Unit)

  fun updateActivity(activity: Activity, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteActivityById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
