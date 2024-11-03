package com.github.se.travelpouch.model

import com.github.se.travelpouch.model.TravelContainerMock.generateAutoObjectId
import com.github.se.travelpouch.model.TravelContainerMock.generateAutoUserId
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
    val user1ID = generateAutoUserId()
    val user2ID = generateAutoUserId()
    val travelID = generateAutoObjectId()
    val participants: MutableMap<Participant, Role> = HashMap()
    participants[Participant(user1ID)] = Role.OWNER
    val travelContainer =
        TravelContainer(
            travelID,
            "Test Title",
            "Test Description",
            Timestamp(1234567890L - 1, 0),
            Timestamp(1234567890L, 0),
            location,
            attachments,
            participants)

    assertEquals(travelID, travelContainer.fsUid)
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
    val user1ID = generateAutoUserId()
    val user2ID = generateAutoUserId()
    val travelId = generateAutoObjectId()
    participants[Participant(user1ID)] = Role.OWNER

    val travelContainer =
        TravelContainer(
            travelId,
            "Test Title",
            "Test Description",
            Timestamp(1234567890L - 1, 0),
            Timestamp(1234567890L, 0),
            location,
            attachments,
            participants)

    val map: Map<String, Any> = travelContainer.toMap()

    assertEquals(travelId, map["fsUid"])
    assertEquals("Test Title", map["title"])
    assertEquals("Test Description", map["description"])
    assertEquals(attachments, map["allAttachments"])

    val locationMap = map["location"] as Map<String, Any>?
    assertEquals(12.34, locationMap!!["latitude"])
    assertEquals(56.78, locationMap["longitude"])
    assertEquals(Timestamp(1234567890L, 0), locationMap["insertTime"])
    assertEquals("Test Location", locationMap["name"])

    val participantsMap = map["allParticipants"] as Map<String, Any>?
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
              generateAutoObjectId(),
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
    val user1ID = generateAutoUserId()
    val user2ID = generateAutoUserId()
    val user3ID = generateAutoUserId()
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
    val user4ID = generateAutoUserId()
    val user5ID = generateAutoUserId()
    val user6ID = generateAutoUserId()
    participants2[Participant(user4ID)] = Role.OWNER
    participants2[Participant(user5ID)] = Role.ORGANIZER
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              generateAutoObjectId(),
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
    val user7ID = generateAutoUserId()
    val user8ID = generateAutoUserId()
    val user9ID = generateAutoUserId()
    participants3[Participant(user7ID)] = Role.OWNER
    participants3[Participant(user8ID)] = Role.ORGANIZER
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              generateAutoObjectId(),
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
              generateAutoObjectId(),
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
        assertThrows(IllegalArgumentException::class.java) { Participant("") }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Participant("fsUid123456789012345612345678")
          // 21 characters is too long
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Participant("fsUid1234567890123412345678")
          // 19 characters is too short
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Participant("fsUid12345678901-23412345678")
          // contains a non-alphanumeric character
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Participant("?sUid123456789012345612345678")
          // verifies it starts with alphanumeric
          //
        }
    assertEquals("Invalid fsUid format", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Participant("sUid1234567890123456____12345678")
          // verifies it ends after 20 alphanumeric characters
          //
        }
    assertEquals("Invalid fsUid format", exception.message)
  }

  @Test
  fun testInvalidLocation() {

    var exception: Exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(12.34, 56.78, Timestamp(1234567890L, 0), "")
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
          Location(-90.0001, 56.78, Timestamp(1234567890L, 0), "hello")
          // too negative latitude
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(90.0001, 56.78, Timestamp(1234567890L, 0), "hello")
          // too positive latitude
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(Double.MIN_VALUE, 56.78, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(Double.MAX_VALUE, 56.78, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(Double.NaN, 56.78, Timestamp(1234567890L, 0), "hello")
          // Nan cases
        }
    assertEquals("Latitude must be between -90.0 and 90.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(12.34, -180.0001, Timestamp(1234567890L, 0), "hello")
          // longitude too negative
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(12.34, 180.0001, Timestamp(1234567890L, 0), "hello")
          // longitude too positive
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(12.34, Double.MAX_VALUE, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(12.34, Double.MIN_VALUE, Timestamp(1234567890L, 0), "hello")
          // edge case
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)

    exception =
        assertThrows(IllegalArgumentException::class.java) {
          Location(12.34, Double.NaN, Timestamp(1234567890L, 0), "hello")
          // Nan cases
        }
    assertEquals("Longitude must be between -180.0 and 180.0", exception.message)
  }
}
