package com.github.se.travelpouch.screen

// import com.github.se.travelpouch.model.Screen
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelContainerMock
import com.github.se.travelpouch.ui.home.MapScreen
import com.github.se.travelpouch.ui.home.TravelListScreen
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class TravelListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun displayTextWhenEmpty() {

    // Act
    composeTestRule.setContent {
      val listTravelViewModel: ListTravelViewModel =
          viewModel(factory = ListTravelViewModel.Factory)
      TravelListScreen(
          navigationActions = navigationActions, travelContainers = listTravelViewModel)
    }

    // Assert
    composeTestRule.onNodeWithTag("emptyTravelPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TravelListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    // Act
    composeTestRule.setContent {
      val listTravelViewModel: ListTravelViewModel =
          viewModel(factory = ListTravelViewModel.Factory)
      TravelListScreen(
          navigationActions = navigationActions, travelContainers = listTravelViewModel)
    }

    // Assert
    composeTestRule.onNodeWithTag("TravelListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun displayTravelListWhenNotEmpty() {

    // Act
    composeTestRule.setContent {
      val listTravelViewModel: ListTravelViewModel =
          viewModel(factory = ListTravelViewModel.Factory)
      TravelListScreen(
          navigationActions = navigationActions, travelContainers = listTravelViewModel)
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
    composeTestRule.setContent { MapScreen(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("TravelListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }
}
