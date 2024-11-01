package com.github.se.travelpouch.ui.dashboard

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.model.events.Event
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventType
import com.github.se.travelpouch.model.events.EventViewModel
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class TimelineScreenTest {

  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockEventViewModel: EventViewModel

  @get:Rule val composeTestRule = createComposeRule()

  /**
   * events_test is a mocking of the list of event that is normally returned by firebase. Thus, when
   * calling getEvents, it is this list that is being returned.
   */
  val events_test =
      listOf(
          Event(
              "1",
              EventType.NEW_DOCUMENT,
              Timestamp(0, 0),
              "eventTitle",
              "eventDescription",
              null,
              null),
          Event("2", EventType.START_OF_JOURNEY, Timestamp(0, 0), "it", "it", null, null),
          Event("3", EventType.NEW_PARTICIPANT, Timestamp(0, 0), "it", "it", null, null),
          Event("3", EventType.OTHER_EVENT, Timestamp(0, 0), "it", "it", null, null))

  @Before
  fun setUp() {
    mockEventRepository = mock(EventRepository::class.java)
    mockEventViewModel = EventViewModel(mockEventRepository)

    // `when`(mockEventViewModel.events.value).thenReturn(events_test)

  }

  @Test
  fun everythingDisplayed() {
    composeTestRule.setContent { TimelineScreen(mockEventViewModel) }

    `when`(mockEventRepository.getEvents(any(), any())).then {
      it.getArgument<(List<Event>) -> Unit>(0)(events_test)
    }

    mockEventViewModel.getEvents()

    composeTestRule.onAllNodes(hasTestTag("boxContainingEvent")).apply {
      fetchSemanticsNodes().forEachIndexed { i, _ -> get(i).assertIsDisplayed() }
    }

    composeTestRule.onAllNodes(hasTestTag("eventCard")).apply {
      fetchSemanticsNodes().forEachIndexed { i, _ -> get(i).assertIsDisplayed() }
    }
  }

  @Test
  fun testCardIsDisplaysCorrectly() {
    composeTestRule.setContent { TimelineItem(events_test[0], Modifier) }

    composeTestRule.onNodeWithTag("eventType").assertIsDisplayed()
    composeTestRule.onNodeWithTag("eventType").assertTextEquals("NEW_DOCUMENT")

    composeTestRule.onNodeWithTag("eventTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("eventTitle").assertTextEquals("eventTitle")

    composeTestRule.onNodeWithTag("eventDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("eventDate").assertTextEquals("1/1/1970")
  }
}
