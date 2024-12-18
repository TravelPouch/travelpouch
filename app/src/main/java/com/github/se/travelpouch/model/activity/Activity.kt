// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.activity

import com.github.se.travelpouch.model.documents.DocumentContainer
import com.github.se.travelpouch.model.travels.Location
import com.google.firebase.Timestamp

/**
 * This class represents an activity in the travel, so a location to go at a given time, with some
 * necessary documents.
 *
 * @property uid (String) : the unique identifier of the activity
 * @property title (String) : the title of the activity
 * @property description (String) : the description of the travel
 * @property location (Location) : the location where the activity takes place
 * @property date (Timestamp) : the date when the activity will occur
 * @property documentsNeeded (List<DocumentContainer>) : the list of documents needed for the
 *   activity.
 */
data class Activity(
    val uid: String,
    val title: String,
    val description: String,
    val location: Location,
    val date: Timestamp,
    val documentsNeeded: List<DocumentContainer> = emptyList()
)
