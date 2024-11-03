package com.github.se.travelpouch.model

import com.google.firebase.Timestamp
import java.util.Date

typealias fsUid = String

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
    val fsUid: fsUid, // Firestore UID
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
    require(isValidObjectUid(fsUid)) { "Invalid fsUid format" }
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

/**
 * Function to check if a Firestore UID is valid. Firestore UID must be 20 characters long and
 * contain only alphanumeric characters.
 *
 * Follows the format specified by random uid generator of firestore
 * [source](https://github.com/firebase/firebase-android-sdk/blob/a024da69daa0a5264b133d9550d1cf068ec2b3ee/firebase-firestore/src/main/java/com/google/firebase/firestore/util/Util.java#L35).
 *
 * @param fsUid Firestore UID to check.
 * @return True if the UID is valid, false otherwise.
 */
fun isValidObjectUid(fsUid: fsUid): Boolean {
  return fsUid.isNotBlank() && fsUid.matches(Regex("^[a-zA-Z0-9]{20}$"))
}

fun isValidUserUid(fsUid: fsUid): Boolean {
  return fsUid.isNotBlank() && fsUid.matches(Regex("^[a-zA-Z0-9]{28}$"))
}

/**
 * Data class representing a participant.
 *
 * @property fsUid Firestore UID of the participant.
 */
data class Participant(
    val fsUid: fsUid, // Firestore UID
) {
  init {
    require(isValidUserUid(fsUid)) { "Invalid fsUid format" }
  }
}

data class UserInfo(
    val fsUid: fsUid,
    val name: String,
    val userTravelList: List<fsUid>,
    val email: String,
) {
  init {
    require(isValidUserUid(fsUid)) { "Invalid fsUid format for fsUid" }
  }

  fun toMap(): Map<String, Any> {
    return mapOf(
        "fsUid" to fsUid, "name" to name, "userTravelList" to userTravelList, "email" to email)
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

/*
 * Mock data generator for TravelContainer.
 */
object TravelContainerMock {

  /**
   * Generates a random alphanumeric string of length 20.
   *
   * @return A randomly generated alphanumeric string.
   */
  fun generateAutoObjectId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..20).map { chars.random() }.joinToString("")
  }

  fun generateAutoUserId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..28).map { chars.random() }.joinToString("")
  }

  /**
   * Creates a mock TravelContainer object with default or provided values.
   *
   * @param fsUid The unique identifier for the travel container. Defaults to a generated ID.
   * @param title The title of the travel container. Defaults to "Mock Travel".
   * @param description The description of the travel container. Defaults to "This is a mock travel
   *   container".
   * @param startTime The start time of the travel container. Defaults to the current timestamp.
   * @param endTime The end time of the travel container. Defaults to one day after the current
   *   timestamp.
   * @param location The location of the travel container. Defaults to EPFL coordinates.
   * @param allAttachments A map of attachment names to their UIDs. Defaults to a single mock
   *   attachment.
   * @param allParticipants A map of participants to their roles. Defaults to a single owner
   *   participant.
   * @return A mock TravelContainer object.
   */
  fun createMockTravelContainer(
      fsUid: String = generateAutoObjectId(),
      title: String = "Mock Travel",
      description: String = "This is a mock travel container",
      startTime: Timestamp = Timestamp.now(),
      endTime: Timestamp = Timestamp(Date(Timestamp.now().toDate().time + 86400000)), // Add one day
      location: Location = Location(46.5191, 6.5668, Timestamp.now(), "EPFL"),
      allAttachments: Map<String, String> = mapOf("Attachment1" to "mockAttachmentUid1"),
      allParticipants: Map<Participant, Role> =
          mapOf(Participant(generateAutoUserId()) to Role.OWNER)
  ): TravelContainer {
    return TravelContainer(
        fsUid = fsUid,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        allAttachments = allAttachments,
        allParticipants = allParticipants)
  }

  /**
   * Creates a list of mock TravelContainer objects.
   *
   * @param size The number of mock TravelContainer objects to create.
   * @param fsUidGenerator A function to generate unique identifiers for the travel containers.
   *   Defaults to generateAutoId.
   * @param titleGenerator A function to generate titles for the travel containers. Defaults to
   *   "Mock Travel {index}".
   * @param descriptionGenerator A function to generate descriptions for the travel containers.
   *   Defaults to "This is mock travel container {index}".
   * @param startTimeGenerator A function to generate start times for the travel containers.
   *   Defaults to the current timestamp.
   * @param endTimeGenerator A function to generate end times for the travel containers. Defaults to
   *   one day after the start time.
   * @param locationGenerator A function to generate locations for the travel containers. Defaults
   *   to EPFL coordinates.
   * @param allAttachmentsGenerator A function to generate attachment maps for the travel
   *   containers. Defaults to a single mock attachment.
   * @param allParticipantsGenerator A function to generate participant maps for the travel
   *   containers. Defaults to a single owner participant.
   * @return A list of mock TravelContainer objects.
   */
  fun createMockTravelContainersList(
      size: Int,
      fsUidGenerator: () -> String = ::generateAutoObjectId,
      titleGenerator: (Int) -> String = { "Mock Travel $it" },
      descriptionGenerator: (Int) -> String = { "This is mock travel container $it" },
      startTimeGenerator: (Int) -> Timestamp = { Timestamp.now() },
      endTimeGenerator: (Int) -> Timestamp = { index ->
        Timestamp(Date(startTimeGenerator(index).toDate().time + 86400000)) // Add one day
      },
      locationGenerator: (Int) -> Location = { Location(46.5191, 6.5668, Timestamp.now(), "EPFL") },
      allAttachmentsGenerator: (Int) -> Map<String, String> = { index ->
        mapOf("Attachment$index" to "mockAttachmentUid$index")
      },
      allParticipantsGenerator: (Int) -> Map<Participant, Role> = {
        mapOf(Participant(generateAutoUserId()) to Role.OWNER)
      }
  ): List<TravelContainer> {
    return (1..size).map { index ->
      createMockTravelContainer(
          fsUid = fsUidGenerator(),
          title = titleGenerator(index),
          description = descriptionGenerator(index),
          startTime = startTimeGenerator(index),
          endTime = endTimeGenerator(index),
          location = locationGenerator(index),
          allAttachments = allAttachmentsGenerator(index),
          allParticipants = allParticipantsGenerator(index))
    }
  }
}
