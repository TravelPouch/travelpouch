package com.github.se.travelpouch.model

object FirebasePaths {
    val TravelsSuperCollection = "allTravels"
    val ProfilesSuperCollection = "profiles"

    val notifications = "notifications"
    val documents = "documents"
    val activities = "activities"
    val events = "events"

    fun constructPath(vararg paths: String): String{
        var completePath = ""
        for (s in paths)
            completePath += "$s/"
        return completePath.dropLast(1)
    }
}