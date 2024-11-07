package com.github.se.travelpouch.model

object FirebasePaths {
  val TravelsSuperCollection = "allTravels"
  val ProfilesSuperCollection = "userslist"

  val notifications = "notifications"
  val documents = "documents"
  val activities = "activities"
  val events = "events"

  fun constructPath(vararg paths: String): String {
    return paths.joinToString("/")
  }
}
