package com.github.se.travelpouch.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class AddTravelScreenTest {

  private lateinit var travelRepository: TravelRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mocking objects for ViewModel and NavigationActions
    travelRepository = mock(TravelRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    listTravelViewModel = ListTravelViewModel(travelRepository)

    // Mock the current route to be the add travel screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.AUTH)
    `when`(listTravelViewModel.getNewUid()).thenReturn("validMockUid12345678")
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("addTravelScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertTextEquals("Create a new travel")
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").assertTextEquals("Save")
    composeTestRule.onNodeWithTag("travelSaveButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputTravelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLocationName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLatitude").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLongitude").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelStartDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelEndDate").assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidStartDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input invalid dates
    inputText("inputTravelStartDate", "notadate")
    inputText("inputTravelEndDate", "20/10/2024")

    // Input valid location
    inputText("inputTravelLocationName", "Paris")
    inputText("inputTravelLatitude", "48.8566")
    inputText("inputTravelLongitude", "2.3522")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithEndInvalidDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input invalid dates
    inputText("inputTravelStartDate", "10/10/2024")
    inputText("inputTravelEndDate", "notadate")

    // Input valid location
    inputText("inputTravelLocationName", "Paris")
    inputText("inputTravelLatitude", "48.8566")
    inputText("inputTravelLongitude", "2.3522")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithInvalidStartAndEndDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input valid dates
    inputText("inputTravelStartDate", "notadate")
    inputText("inputTravelEndDate", "notadate")

    // Input valid location
    inputText("inputTravelLocationName", "Paris")
    inputText("inputTravelLatitude", "48.8566")
    inputText("inputTravelLongitude", "2.3522")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithNonNumericDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input invalid dates
    inputText("inputTravelStartDate", "notadate/10/2024")
    inputText("inputTravelEndDate", "notadate/10/2024")

    // Input valid location
    inputText("inputTravelLocationName", "Paris")
    inputText("inputTravelLatitude", "48.8566")
    inputText("inputTravelLongitude", "2.3522")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithFalseLocation() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input valid dates
    inputText("inputTravelStartDate", "10/10/2024")
    inputText("inputTravelEndDate", "20/10/2024")

    // Input invalid location
    inputText("inputTravelLocationName", "Paris")
    inputText("inputTravelLatitude", "invalid_latitude")
    inputText("inputTravelLongitude", "2.3522")

    // Simulate clicking the save button
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel due to invalid location
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun submitTravelWithValidData() {

    // Set up the content for the test
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    composeTestRule.waitForIdle() // Ensures inputs are registered

    // Input valid travel details
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")
    inputText("inputTravelStartDate", "10/10/2024")
    inputText("inputTravelEndDate", "20/10/2024")
    inputText("inputTravelLocationName", "Paris")
    inputText("inputTravelLatitude", "48.8566")
    inputText("inputTravelLongitude", "2.3522")

    composeTestRule.waitForIdle() // Ensures inputs are registered

    // Simulate clicking the save button
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is called to add a travel
    verify(travelRepository).addTravel(any(), any(), any())
  }

  @Test
  fun backButtonNavigatesCorrectly() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Click the go back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify that the goBack function is called
    verify(navigationActions).goBack()
  }

  @Test
  fun saveButtonNotEnabled() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Initially, the save button should be disabled
    composeTestRule.onNodeWithTag("travelSaveButton").assertIsNotEnabled()
  }

  // Helper function to input text into a text field
  fun inputText(testTag: String, text: String) {
    composeTestRule.onNodeWithTag(testTag).performTextClearance()
    composeTestRule.onNodeWithTag(testTag).performTextInput(text)
  }
}
