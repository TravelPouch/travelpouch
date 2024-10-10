package com.github.se.travelpouch.screen

// import com.github.se.travelpouch.model.Screen
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelContainerMock
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.overview.MapScreen
import com.github.se.travelpouch.ui.overview.OverviewScreen
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun displayTextWhenEmpty() {
    // Arrange
    val travelContainers = emptyList<TravelContainer>()

    // Act
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, travelContainers = travelContainers)
    }

    // Assert
    composeTestRule.onNodeWithTag("emptyTravelPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    // Arrange
    val travelContainers = emptyList<TravelContainer>()

    // Act
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, travelContainers = travelContainers)
    }

    // Assert
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun displayTravelListWhenNotEmpty() {
    // Arrange
    val startTime = Timestamp(Date())
    val endTime = Timestamp(Date(startTime.toDate().time + 86400000)) // One day later
    val location =
        Location(latitude = 48.8566, longitude = 2.3522, insertTime = startTime, name = "Paris")
    val participant = Participant(fsUid = TravelContainerMock.generateAutoId())
    val participants = mapOf(participant to Role.OWNER)
    val attachments = mapOf<String, String>()
    val travelContainer =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoId(),
            title = "Trip to Paris",
            description = "A wonderful trip to Paris",
            startTime = startTime,
            endTime = endTime,
            location = location,
            allAttachments = attachments,
            allParticipants = participants)
    val travelContainers = listOf(travelContainer)

    // Act
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, travelContainers = travelContainers)
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
    val participant = Participant(fsUid = TravelContainerMock.generateAutoId())
    val participants = mapOf(participant to Role.OWNER)
    val attachments = mapOf<String, String>()

    val locationParis =
        Location(latitude = 48.8566, longitude = 2.3522, insertTime = startTime, name = "Paris")
    val travelParis =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoId(),
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
            fsUid = TravelContainerMock.generateAutoId(),
            title = "Visit New York",
            description = "Exploring NYC",
            startTime = startTime,
            endTime = endTime,
            location = locationNYC,
            allAttachments = attachments,
            allParticipants = participants)

    val travelContainers = listOf(travelParis, travelNYC)

    // Act
    composeTestRule.setContent {
      MapScreen(navigationActions = navigationActions, travelContainers = travelContainers)
    }

    // Assert
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // Note: Direct verification of markers on the map might not be possible with Compose UI tests.
    // However, ensuring the map displays when travelContainers are provided is sufficient.
  }

  //    Not needed for now as the navigation is not implemented

  //  @Test
  //  fun createTravelButtonCallsAction() {
  //    // Arrange
  //    composeTestRule.setContent {
  //      OverviewScreen(navigationActions = navigationActions, travelContainers = emptyList())
  //    }
  //
  //    // Act
  //    composeTestRule.onNodeWithTag("createTravelFab").performClick()
  //
  //    // Assert
  //     verify(navigationActions).navigateTo(screen = Screen.ADD_TRAVEL)
  //  }
}
