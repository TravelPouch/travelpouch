package com.github.se.travelpouch.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.github.se.travelpouch.helper.FileDownloader
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentRepository
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelContainerMock
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TravelListScreenRotationTest {

  // @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel
  private lateinit var travelRepository: TravelRepository
  private lateinit var activityViewModel: ActivityViewModel
  private lateinit var activityRepository: ActivityRepository
  private lateinit var documentViewModel: DocumentViewModel
  private lateinit var documentRepository: DocumentRepository
  private lateinit var eventViewModel: EventViewModel
  private lateinit var eventRepository: EventRepository

  @Mock private lateinit var mockFileDownloader: FileDownloader

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    travelRepository = mock(TravelRepository::class.java)
    activityRepository = mock(ActivityRepository::class.java)
    documentRepository = mock(DocumentRepository::class.java)
    eventRepository = mock(EventRepository::class.java)
    mockFileDownloader = mock()

    listTravelViewModel = ListTravelViewModel(travelRepository)
    activityViewModel = ActivityViewModel(activityRepository)
    eventViewModel = EventViewModel(eventRepository)
    documentViewModel = DocumentViewModel(documentRepository, mockFileDownloader)

    // Mock the repository methods
    val participant = Participant(fsUid = TravelContainerMock.generateAutoUserId())
    val participants = mapOf(participant to Role.OWNER)
    val travelList =
        listOf(
            TravelContainer(
                fsUid = TravelContainerMock.generateAutoObjectId(),
                title = "Trip to Paris",
                description = "A wonderful trip to Paris",
                startTime = Timestamp(Date()),
                endTime = Timestamp(Date(Timestamp(Date()).toDate().time + 86400000)),
                location =
                    Location(
                        latitude = 48.8566,
                        longitude = 2.3522,
                        insertTime = Timestamp.now(),
                        name = "Paris"),
                allAttachments = emptyMap(),
                allParticipants = participants,
                emptyList()))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<TravelContainer>) -> Unit
          onSuccess(travelList)
          null
        }
        .whenever(travelRepository)
        .getTravels(anyOrNull(), anyOrNull())

    // Initialize the ViewModel's travels StateFlow
    listTravelViewModel =
        ListTravelViewModel(travelRepository).apply {} // travels.value = travelList }
  }

  @Test
  fun travelListScreen_rotate() {
    val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    // Rotate the screen to landscape
    uiDevice.setOrientationLeft()

    // Wait for the UI to settle after rotation
    uiDevice.waitForIdle(3000) // Waits up to 3 seconds

    // Set up the content within the activity's context
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions,
          listTravelViewModel = listTravelViewModel,
          activityViewModel,
          eventViewModel,
          documentViewModel)
    }

    // Assert that the UI components are still displayed correctly
    composeTestRule.onNodeWithTag("TravelListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()

    // Reset the orientation after the test
    uiDevice.unfreezeRotation()

    // Wait for the UI to settle after rotation
    uiDevice.waitForIdle(3000) // Waits up to 3 seconds

    composeTestRule.waitForIdle()
  }
}
