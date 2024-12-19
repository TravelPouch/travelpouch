// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.events

import com.google.firebase.Timestamp

/**
 * A class representing the events of a travel, like the uploading of new documents, joining of new
 * participants and creation of a travel.
 *
 * @param uid (String) : the unique identifier of an event
 * @param eventType (EventType) : the type of an event occurring during a travel
 * @param date (Timestamp) : the date when the event occurred
 * @param title (String) : the title of the event
 * @param description (String) : the description of the event
 */
data class Event(
    val uid: String,
    val eventType: EventType,
    val date: Timestamp,
    val title: String,
    val description: String
)

/** The enum class representing the type of event that can occur. */
enum class EventType {
  START_OF_JOURNEY,
  NEW_PARTICIPANT,
  PARTICIPANT_REMOVED,
  NEW_ACTIVITY
}
