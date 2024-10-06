package com.github.se.travelpouch.model

import com.google.firebase.Timestamp

/**
 * Data class representing a travel container.
 *
 * @property fsUid Firestore UID of the travel container. Cannot be blank.
 * @property title Title of the travel. Cannot be blank.
 * @property description Description of the travel container, can be a blank string.
 * @property startTime Start time of the travel. Cannot be before endTime nor empty.
 * @property endTime End time of the travel. Cannot be empty.
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
    require(isValidUid(fsUid)) { "Invalid fsUid format" }
    require(title.isNotBlank()) { "Title cannot be blank" }
    require(startTime.toDate().before(endTime.toDate())) {
      "startTime must be strictly before endTime"
    }
  }

  /**
   * Function to convert a TravelContainer object to a Map.
   *
   * @return A map representation of the TravelContainer object.
   */
  fun toMap(): Map<String, Any> {
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
        "allParticipants" to allParticipants.mapKeys { it.key.fsUid }.mapValues { it.value.name })
  }
}

fun isValidUid(fsUid: String): Boolean {
  return fsUid.isNotBlank() && fsUid.matches(Regex("[a-zA-Z0-9]{20}"))
}

/**
 * Data class representing a participant.
 *
 * @property fsUid Firestore UID of the participant.
 */
data class Participant(
    val fsUid: String, // Firestore UID
) {
  init {
    require(isValidUid(fsUid)) { "Invalid fsUid format" }
  }
}

/** Enum class representing the role of a participant. */
enum class Role {
  OWNER,
  ORGANIZER,
  PARTICIPANT
}

/**
 * Data class representing a location.
 *
 * @property latitude Latitude of the location. Must be between -90.0 and 90.0 included
 * @property longitude Longitude of the location. Must be between -180.0 and 180.0 included
 * @property insertTime Timestamp when the location was inserted.
 * @property name Name of the location. Cannot be blank.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val insertTime: Timestamp,
    val name: String,
) {
  init {
    // Double.MIN_VALUE has to be checked manually because otherwise it doesn't catch it
    require(latitude in -90.0..90.0 && latitude != Double.MIN_VALUE) {
      "Latitude must be between -90.0 and 90.0"
    }
    require(longitude in -180.0..180.0 && longitude != Double.MIN_VALUE) {
      "Longitude must be between -180.0 and 180.0"
    }
    require(name.isNotBlank()) { "Location name cannot be blank" }
  }
}
