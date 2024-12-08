package com.github.se.travelpouch.model.events

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class EventModelViewUnitTest {

  private lateinit var repository: EventRepository
  private lateinit var eventViewModel: EventViewModel

  val event = Event("1", EventType.NEW_DOCUMENT, Timestamp(0, 0), "it", "it", null, null)

  @Before
  fun setUp() {
    repository = mock(EventRepository::class.java)
    eventViewModel = EventViewModel(repository)
  }

  @Test
  fun getEventsTest() {
    eventViewModel.getEvents()
    verify(repository).getEvents(anyOrNull(), anyOrNull())
  }

  //  @Test
  //  fun addEventTest() {
  //    eventViewModel.addEvent(event)
  //    verify(repository).addEvent(anyOrNull(), anyOrNull(), anyOrNull())
  //  }

  @Test
  fun getNewUidTest() {
    val documentReference: DocumentReference = mock()

    `when`(repository.getNewDocumentReferenceForNewTravel(anyOrNull()))
        .thenReturn(documentReference)
    assertThat(
        eventViewModel.getNewDocumentReferenceForNewTravel("qwertzuiopasdfghjkly"),
        `is`(documentReference))
  }
}
