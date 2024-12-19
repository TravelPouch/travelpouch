// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
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
