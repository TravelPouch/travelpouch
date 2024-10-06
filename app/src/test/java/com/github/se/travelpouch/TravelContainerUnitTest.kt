package com.github.se.travelpouch

import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.toMap
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TravelContainerUnitTest {
  @Test
  fun testTravelContainerCreation() {
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"
    val participants: MutableMap<Participant, Role> = HashMap()
    participants[Participant("Participant1")] = Role.OWNER
    val travelContainer =
        TravelContainer(
            "fsUid1",
            "Test Title",
            "Test Description",
            Timestamp(1234567890L, 0),
            Timestamp(1234567890L, 0),
            location,
            attachments,
            participants)

    assertEquals("fsUid1", travelContainer.fsUid)
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
    participants[Participant("Participant1")] = Role.OWNER

    val travelContainer =
        TravelContainer(
            "fsUid1",
            "Test Title",
            "Test Description",
            Timestamp(1234567890L, 0),
            Timestamp(1234567890L, 0),
            location,
            attachments,
            participants)

    val map: Map<String, Any> = travelContainer.toMap()

    assertEquals("fsUid1", map["fsUid"])
    assertEquals("Test Title", map["title"])
    assertEquals("Test Description", map["description"])
    assertEquals(attachments, map["allAttachments"])

    val locationMap = map["location"] as Map<String, Any>?
    assertEquals(12.34, locationMap!!["latitude"])
    assertEquals(56.78, locationMap!!["longitude"])
    assertEquals(Timestamp(1234567890L, 0), locationMap!!["insertTime"])
    assertEquals("Test Location", locationMap!!["name"])

    val participantsMap = map["allParticipants"] as Map<String, String>?
    assertEquals("OWNER", participantsMap!!["Participant1"])
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
              "fsUid1",
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
    participants[Participant("Participant1")] = Role.PARTICIPANT
    participants[Participant("Participant2")] = Role.ORGANIZER
    exception =
        assertThrows(IllegalArgumentException::class.java) {
          TravelContainer(
              "fsUid1",
              "Test Title",
              "Test Description",
              Timestamp(1234567890L, 0),
              Timestamp(1234567890L, 0),
              location,
              attachments,
              participants)
        }
    assertEquals("At least one owner is required", exception.message)
  }
}
