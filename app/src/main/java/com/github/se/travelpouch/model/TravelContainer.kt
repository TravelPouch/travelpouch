package com.github.se.travelpouch.model

import com.google.firebase.Timestamp

/**
 * Data class representing a travel container.
 *
 * @property fsUid Firestore UID of the travel container.
 * @property title Title of the travel.
 * @property description Description of the travel container, can be a blank string.
 * @property startTime Start time of the travel.
 * @property endTime End time of the travel.
 * @property location Location details of the travel.
 * @property allAttachments Map of attachments, name of the attachment and Firestore UID of the
 *   attachment.
 * @property allParticipants Map of participants, Participant and their Role. One owner must exist
 *   at all times unless the travel container is deleted.
 */
data class TravelContainer(
    val fsUid: String, // Firestore UID
    val title: String,
    val description: String, // can be a blank string
    val startTime: Timestamp,
    val endTime: Timestamp,
    val location: Location,
    val allAttachments:
        Map<
            String,
            String>, // list of attachments, key is the name of the attachment, value is the uid of
    // the attachment
    val allParticipants:
        Map<
            Participant,
            Role>, // One owner must exist at all times unless travel container is deleted
) {
  init {
    require(allParticipants.isNotEmpty()) { "At least one participant is required" }
    require(allParticipants.values.contains(Role.OWNER)) { "At least one owner is required" }
  }
}

/**
 * Extension function to convert a TravelContainer object to a Map.
 *
 * @return A map representation of the TravelContainer object.
 */
fun TravelContainer.toMap(): Map<String, Any> {
  return mapOf(
      "fsUid" to fsUid,
      "title" to title,
      "description" to description,
      "startTime" to startTime,
      "endTime" to endTime,
      "location" to
          mapOf(
              "latitude" to location.latitude,
              "longitude" to location.longitude,
              "insertTime" to location.insertTime,
              "name" to location.name),
      "allAttachments" to allAttachments,
      "allParticipants" to allParticipants.mapKeys { it.key.fsUId }.mapValues { it.value.name })
}

/**
 * Data class representing a participant.
 *
 * @property fsUId Firestore UID of the participant.
 */
data class Participant(
    val fsUId: String, // Firestore UID
)

/** Enum class representing the role of a participant. */
enum class Role {
  OWNER,
  ORGANIZER,
  PARTICIPANT
}

/**
 * Data class representing a location.
 *
 * @property latitude Latitude of the location.
 * @property longitude Longitude of the location.
 * @property insertTime Timestamp when the location was inserted.
 * @property name Name of the location.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val insertTime: Timestamp,
    val name: String,
)
