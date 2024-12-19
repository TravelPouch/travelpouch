// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.travel

import com.github.se.travelpouch.di.travelCollection
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelContainerMock
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.model.travels.TravelRepositoryMock
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import junit.framework.TestCase.assertFalse
import org.junit.Test
import org.mockito.Mockito.mock

class TravelRepositoryMockTest {

  val travel =
      TravelContainer(
          "qwertzuiopasdfghjkly",
          "title",
          "description",
          Timestamp(0, 0),
          Timestamp.now(),
          Location(0.0, 0.0, Timestamp(0, 0), "name"),
          emptyMap(),
          mapOf(Participant(TravelContainerMock.generateAutoUserId()) to Role.OWNER),
          emptyList())

  val travelUpdated =
      TravelContainer(
          "qwertzuiopasdfghjkly",
          "title - modified",
          "description",
          Timestamp(0, 0),
          Timestamp.now(),
          Location(0.0, 0.0, Timestamp(0, 0), "name"),
          emptyMap(),
          mapOf(Participant(TravelContainerMock.generateAutoUserId()) to Role.OWNER),
          emptyList())

  val travelMockRepository = TravelRepositoryMock()
  val eventDocumentReference: DocumentReference = mock()

  @Test
  fun verifiesThatAddingWorks() {
    var succeeded = false
    var failed = false

    val noTravel = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(noTravel == null)
    travelMockRepository.addTravel(
        travel, { succeeded = true }, { failed = true }, eventDocumentReference)
    assert(succeeded)
    assertFalse(failed)

    val newTravel = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(newTravel == travel)

    com.github.se.travelpouch.di.travelCollection.clear()
  }

  @Test
  fun verifiesThatUpdatingWorks() {
    var succeeded = false
    var failed = false

    val noTravel = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(noTravel == null)
    travelMockRepository.addTravel(travel, {}, {}, eventDocumentReference)
    val travelAdded = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(travelAdded == travel)
    travelMockRepository.updateTravel(
        travelUpdated,
        TravelRepository.UpdateMode.FIELDS_UPDATE,
        null,
        { succeeded = true },
        { failed = true },
        null)
    assert(succeeded)
    assertFalse(failed)

    val newTravel = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(newTravel == travelUpdated)

    com.github.se.travelpouch.di.travelCollection.clear()
  }

  @Test
  fun verifiesThatDeletingWorks() {
    var succeeded = false
    var failed = false

    val noTravel = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(noTravel == null)
    travelMockRepository.addTravel(travel, {}, {}, eventDocumentReference)
    val travelAdded = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(travelAdded == travel)
    travelMockRepository.deleteTravelById(travel.fsUid, { succeeded = true }, { failed = true })
    assert(succeeded)
    assertFalse(failed)

    val newTravel = com.github.se.travelpouch.di.travelCollection[travel.fsUid]
    assert(newTravel == null)

    com.github.se.travelpouch.di.travelCollection.clear()
  }
}
