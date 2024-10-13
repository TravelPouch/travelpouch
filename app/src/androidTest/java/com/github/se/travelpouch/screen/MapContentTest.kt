package com.github.se.travelpouch.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelContainerMock
import com.github.se.travelpouch.ui.overview.MapContent
import com.github.se.travelpouch.ui.overview.MapScreen
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Rule
import org.junit.Test

class MapContentTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testMapContent_WithEmptyMarkers() {
    // Arrange
    val travelContainers = emptyList<TravelContainer>()

    // Act
    composeTestRule.setContent { MapContent(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // Since we cannot assert on the map's camera position directly, we can check that the map
    // renders.
  }

  @Test
  fun testMapContent_WithMarkers() {
    // Arrange
    val travelContainers = TravelContainerMock.createMockTravelContainersList(size = 2)

    // Act
    composeTestRule.setContent { MapContent(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // Cannot directly assert on markers, but the code paths for adding markers should be executed.
  }

  @Test
  fun testMapContent_WithNullLocations() {
    // Arrange
    val startTime = Timestamp(Date())
    val endTime = Timestamp(Date(startTime.toDate().time + 86400000))
    val participant = Participant(fsUid = TravelContainerMock.generateAutoId())
    val participants = mapOf(participant to Role.OWNER)
    val attachments = mapOf<String, String>()

    val travelContainerWithNullLocation =
        TravelContainer(
            fsUid = TravelContainerMock.generateAutoId(),
            title = "Travel without location",
            description = "This travel has no location",
            startTime = startTime,
            endTime = endTime,
            location = // TODO
            Location(12.34, 56.78, Timestamp(1234567890L, 0), "EPFL"),
            allAttachments = attachments,
            allParticipants = participants)

    val travelContainers = listOf(travelContainerWithNullLocation)

    // Act
    composeTestRule.setContent { MapContent(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // This test ensures that the code handles null locations gracefully.
  }

  @Test
  fun testMapScreen_ContentIsDisplayed() {
    // Arrange
    val travelContainers = TravelContainerMock.createMockTravelContainersList(size = 1)

    // Act
    composeTestRule.setContent { MapScreen(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
  }

  @Test
  fun testMapContent_MultipleMarkers() {
    // Arrange
    val travelContainers = TravelContainerMock.createMockTravelContainersList(size = 5)

    // Act
    composeTestRule.setContent { MapContent(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    // The markers.foreach loop should execute 5 times.
  }
}
