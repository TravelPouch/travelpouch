// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelContainerMock
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
  fun testMapScreen_ContentIsDisplayed() {
    // Arrange
    val travelContainers = TravelContainerMock.createMockTravelContainersList(size = 1)

    // Act
    composeTestRule.setContent { MapScreen(travelContainers = travelContainers) }

    // Assert
    composeTestRule.onNodeWithTag("listTravelScreen").assertIsDisplayed()
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
