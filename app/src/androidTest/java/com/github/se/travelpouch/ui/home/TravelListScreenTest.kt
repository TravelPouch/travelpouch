import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelContainerMock
import com.github.se.travelpouch.model.TravelRepository
import com.github.se.travelpouch.ui.home.MapScreen
import com.github.se.travelpouch.ui.home.TravelListScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TravelListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel
  private lateinit var travelRepository: TravelRepository

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    travelRepository = mock(TravelRepository::class.java)
    listTravelViewModel = ListTravelViewModel(travelRepository)

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
                allParticipants = participants))

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
  fun hasRequiredComponents() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions, listTravelViewModel = listTravelViewModel)
    }
    Thread.sleep(3000)
    // Assert
    composeTestRule.onNodeWithTag("TravelListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun displayTravelListWhenNotEmpty() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions, listTravelViewModel = listTravelViewModel)
    }

    // Assert
    composeTestRule.onNodeWithTag("travelListItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trip to Paris").assertIsDisplayed()
    composeTestRule.onNodeWithText("A wonderful trip to Paris").assertIsDisplayed()
  }

  @Test
  fun displayMapWithMarkers() {
    // Arrange
    val startTime = Timestamp(Date())
    val endTime = Timestamp(Date(startTime.toDate().time + 86400000))
    val participant = Participant(fsUid = TravelContainerMock.generateAutoUserId())
    val participants = mapOf(participant to Role.OWNER)
    val attachments = mapOf<String, String>()

    val locationParis =
        Location(latitude = 48.8566, longitude = 2.3522, insertTime = startTime, name = "Paris")
    val travelParis =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoObjectId(),
            title = "Trip to Paris",
            description = "A wonderful trip to Paris",
            startTime = startTime,
            endTime = endTime,
            location = locationParis,
            allAttachments = attachments,
            allParticipants = participants)

    val locationNYC =
        Location(
            latitude = 40.7128, longitude = -74.0060, insertTime = startTime, name = "New York")
    val travelNYC =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoObjectId(),
            title = "Visit New York",
            description = "Exploring NYC",
            startTime = startTime,
            endTime = endTime,
            location = locationNYC,
            allAttachments = attachments,
            allParticipants = participants)

    val travelContainers = listOf(travelParis, travelNYC)

    // Act
    composeTestRule.setContent { MapScreen(travelContainers = travelContainers) }
    composeTestRule.waitForIdle()
    Thread.sleep(3000)

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun testNavigationBottomBar() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions, listTravelViewModel = listTravelViewModel)
    }
    composeTestRule.waitForIdle()

    // Click on the "Activities" navigation item
    composeTestRule.onNodeWithTag("Travels").performClick()
    composeTestRule.waitForIdle()

    // Verify that the navigation action was called for "Activities"
    verify(navigationActions).navigateTo(Screen.TRAVEL_LIST)

    // Click on the "Calendar" navigation item
    composeTestRule.onNodeWithTag("Calendar").performClick()
    composeTestRule.waitForIdle()

    // Verify that the navigation action was called for "Calendar"
    verify(navigationActions).navigateTo(Screen.CALENDAR)
  }

  @Test
  fun testTravelItemClickNavigatesToTravelActivities() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions, listTravelViewModel = listTravelViewModel)
    }
    composeTestRule.waitForIdle()

    // Find and click on a travel list item
    composeTestRule.onNodeWithTag("travelListItem").performClick()
    composeTestRule.waitForIdle()

    // Verify that the navigation action was called for TRAVEL_ACTIVITIES
    verify(navigationActions).navigateTo(Screen.TRAVEL_ACTIVITIES)
  }

  @Test
  fun testCreateTravelFabClickNavigatesToAddTravel() {
    // Act
    composeTestRule.setContent {
      TravelListScreen(
          navigationActions = navigationActions, listTravelViewModel = listTravelViewModel)
    }
    composeTestRule.waitForIdle()

    // Find and click on the create travel FAB
    composeTestRule.onNodeWithTag("createTravelFab").performClick()
    composeTestRule.waitForIdle()

    // Verify that the navigation action was called for ADD_TRAVEL
    verify(navigationActions).navigateTo(Screen.ADD_TRAVEL)
  }
}
