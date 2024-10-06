package com.github.se.travelpouch

import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

fun generateAutoId(): String {
  // follows the format specified by random uid generator of firestore
  // https://github.com/firebase/firebase-android-sdk/blob/a024da69daa0a5264b133d9550d1cf068ec2b3ee/firebase-firestore/src/main/java/com/google/firebase/firestore/util/Util.java#L35
  val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  return (1..20).map { chars.random() }.joinToString("")
}

class TravelContainerUnitTest {
  @Test
  fun testTravelContainerCreation() {
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"
    val user1ID = generateAutoId()
    val user2ID = generateAutoId()
    val participants: MutableMap<Participant, Role> = HashMap()
    participants[Participant(user1ID)] = Role.OWNER
    val travelContainer =
        TravelContainer(
            user2ID,
            "Test Title",
            "Test Description",
            Timestamp(1234567890L - 1, 0),
            Timestamp(1234567890L, 0),
            location,
            attachments,
            participants)

    assertEquals(user2ID, travelContainer.fsUid)
    assertEquals("Test Title", travelContainer.title)
    assertEquals("Test Description", travelContainer.description)
    assertEquals(location, travelContainer.location)
    assertEquals(attachments, travelContainer.allAttachments)
    assertEquals(participants, travelContainer.allParticipants)
  }

  @Test
  fun testTravelContainerToMap() {
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"
    val participants: MutableMap<Participant, Role> = HashMap()
    val user1ID = generateAutoId()
    val user2ID = generateAutoId()
    participants[Participant(user1ID)] = Role.OWNER

    val travelContainer =
        TravelContainer(
            user2ID,
            "Test Title",
            "Test Description",
            Timestamp(1234567890L - 1, 0),
            Timestamp(1234567890L, 0),
            location,
            attachments,
            participants)

    val map: Map<String, Any> = travelContainer.toMap()

    assertEquals(user2ID, map["fsUid"])
    assertEquals("Test Title", map["title"])
    assertEquals("Test Description", map["description"])
    assertEquals(attachments, map["allAttachments"])

    val locationMap = map["location"] as Map<String, Any>?
    assertEquals(12.34, locationMap!!["latitude"])
    assertEquals(56.78, locationMap["longitude"])
    assertEquals(Timestamp(1234567890L, 0), locationMap["insertTime"])
    assertEquals("Test Location", locationMap["name"])

    val participantsMap = map["allParticipants"] as Map<String, String>?
    assertEquals("OWNER", participantsMap!![user1ID])
  }

  @Test
  fun testTravelContainerValidation() {
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"

    // Test with no participants
    var exception: Exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              generateAutoId(),
              "Test Title",
              "Test Description",
              Timestamp(1234567890L, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              HashMap<Participant, Role>())
        }
    assertEquals("At least one participant is required", exception.message)

    // Test with no owner
    val participants: MutableMap<Participant, Role> = HashMap()
    val user1ID = generateAutoId()
    val user2ID = generateAutoId()
    val user3ID = generateAutoId()
    participants[Participant(user1ID)] = Role.PARTICIPANT
    participants[Participant(user2ID)] = Role.ORGANIZER
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              user3ID,
              "Test Title",
              "Test Description",
              Timestamp(1234567890L, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              participants)
        }
    assertEquals("At least one owner is required", exception.message)

    // Test with blank title
    val participants2: MutableMap<Participant, Role> = HashMap()
    val user4ID = generateAutoId()
    val user5ID = generateAutoId()
    val user6ID = generateAutoId()
    participants2[Participant(user4ID)] = Role.OWNER
    participants2[Participant(user5ID)] = Role.ORGANIZER
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              user6ID,
              "",
              "Test Description",
              Timestamp(1234567890L - 1, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              participants2)
        }
    assertEquals("Title cannot be blank", exception.message)

    // Test with startTime after endTime
    val participants3: MutableMap<Participant, Role> = HashMap()
    val user7ID = generateAutoId()
    val user8ID = generateAutoId()
    val user9ID = generateAutoId()
    participants3[Participant(user7ID)] = Role.OWNER
    participants3[Participant(user8ID)] = Role.ORGANIZER
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              user9ID,
              "Title",
              "Test Description",
              Timestamp(1234567890L, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              participants3)
        }
    assertEquals("startTime must be strictly before endTime", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              user9ID,
              "Title",
              "Test Description",
              Timestamp(1234567890L + 1, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              participants3)
        }
    assertEquals("startTime must be strictly before endTime", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              "sigma gyat",
              "Title",
              "Test Description",
              Timestamp(1234567890L - 1, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              participants3)
        }
    assertEquals("Invalid fsUid format", exception.message)
  }

  @Test
  fun testInvalidParticipantUid() {

    var exception: Exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant1 = Participant("")
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant2 = Participant("fsUid1234567890123456")
          // 21 characters is too long
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant3 = Participant("fsUid12345678901234")
          // 19 characters is too short
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant3 = Participant("fsUid12345678901-234")
          // contains a non-alphanumeric character
        }
    assertEquals("Invalid fsUid format", exception.message)
  }

  @Test
  fun testInvalidLocation() {

    var exception: Exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(12.34, 56.78, Timestamp(1234567890L, 0), "")
          // blank location name
        }
    assertEquals("Location name cannot be blank", exception.message)

    // valid location
    val timestamp = Timestamp(1234567890L, 0)
    val location = Location(12.34, 56.78, timestamp, "Test Location")
    assertEquals(location.name, "Test Location")
    assertEquals(location.latitude, 12.34, 0.000001)
    assertEquals(location.longitude, 56.78, 0.000001)
    assertEquals(location.insertTime, timestamp)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(-90.0001, 56.78, Timestamp(1234567890L, 0), "hello")
          // too negative latitude
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(90.0001, 56.78, Timestamp(1234567890L, 0), "hello")
          // too positive latitude
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(Double.MIN_VALUE, 56.78, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(Double.MAX_VALUE, 56.78, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(Double.NaN, 56.78, Timestamp(1234567890L, 0), "hello")
          // Nan cases
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(12.34, -180.0001, Timestamp(1234567890L, 0), "hello")
          // longitude too negative
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(12.34, 180.0001, Timestamp(1234567890L, 0), "hello")
          // longitude too positive
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(12.34, Double.MAX_VALUE, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(12.34, Double.MIN_VALUE, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          val participant = Location(12.34, Double.NaN, Timestamp(1234567890L, 0), "hello")
          // Nan cases
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)
  }
}
