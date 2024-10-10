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
    composeTestRule.onNodeWithTag("travelSaveButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").assertTextEquals("Save")

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

    composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("Trip to Paris")

    // Input invalid date
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("notadate")

    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("20/10/2024")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithEndInvalidDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("Trip to Paris")

    // Input invalid date
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("20/10/2024")

    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("notadate")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithInvalidStartAndEndDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("Trip to Paris")

    // Input invalid date
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("notadate")

    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("notadate")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithNonNumericDate() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("Trip to Paris")

    // Input invalid date
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("notadate/10/2024")

    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("notadate/10/2024")

    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithFalseLocation() {
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid title and description
    composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("Trip to Paris")
    composeTestRule.onNodeWithTag("inputTravelDescription").performTextInput("A fun trip to Paris")

    // Input valid dates
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("10/10/2024")

    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("20/10/2024")

    // Input an invalid latitude (non-numeric)
    composeTestRule.onNodeWithTag("inputTravelLatitude").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelLatitude").performTextInput("invalid_latitude")

    // Input a valid longitude
    composeTestRule.onNodeWithTag("inputTravelLongitude").performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelLongitude").performTextInput("2.3522")

    // Simulate clicking the save button
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    // Verify that the repository method is not called to add a travel due to invalid location
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun submitTravelWithValidData() {

    // Set up the content for the test
    composeTestRule.setContent { AddTravelScreen(listTravelViewModel, navigationActions) }

    // Input valid travel details
    composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("Trip to Paris")
    composeTestRule.onNodeWithTag("inputTravelDescription").performTextInput("A fun trip to Paris")
    composeTestRule.onNodeWithTag("inputTravelLocationName").performTextInput("Paris")
    composeTestRule.onNodeWithTag("inputTravelLatitude").performTextInput("48.8566")
    composeTestRule.onNodeWithTag("inputTravelLongitude").performTextInput("2.3522")
    composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("10/10/2024")
    composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("20/10/2024")

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
}
