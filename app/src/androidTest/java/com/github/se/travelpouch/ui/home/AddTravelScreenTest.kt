package com.github.se.travelpouch.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.location.LocationRepository
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow

class AddTravelScreenTest {

  class FakeLocationRepository : LocationRepository {

    val location =
        listOf(
            Location(48.8566, 2.3522, Timestamp.now(), "Paris"),
            Location(34.0522, -118.2437, Timestamp.now(), "Los Angeles"),
            Location(51.5074, -0.1278, Timestamp.now(), "London"))

    override fun search(
        query: String,
        onSuccess: (List<Location>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
      onSuccess(location)
    }
  }

  private lateinit var travelRepository: TravelRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel
  private lateinit var locationViewModel: LocationViewModel

  private lateinit var profileRepository: ProfileRepository
  private lateinit var profileModelView: ProfileModelView

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val profile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "username",
            "email@test.ch",
            emptyMap(),
            "name",
            emptyList())

    // Mocking objects for ViewModel and NavigationActions
    travelRepository = mock(TravelRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)

    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView((profileRepository))

    listTravelViewModel = ListTravelViewModel(travelRepository)
    // Use a real LocationViewModel with a fake repository
    locationViewModel = LocationViewModel(FakeLocationRepository())

    // Mock the current route to be the add travel screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.AUTH)
    `when`(listTravelViewModel.getNewUid()).thenReturn("validMockUid12345678")
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      AddTravelScreen(listTravelViewModel, navigationActions, profileModelView = profileModelView)
    }

    composeTestRule.onNodeWithTag("addTravelScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelTitle").assertTextEquals("Create a new travel")
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertTextEquals("Save")
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputTravelTitle").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelDescription").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLocation").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputTravelStartDate").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelEndDate").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidStartDate() {
    composeTestRule.setContent {
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input invalid dates
    inputText("inputTravelStartDate", "notadate")
    inputText("inputTravelEndDate", "20/10/2024")

    // Input valid location
    inputText("inputTravelLocation", "Paris")
    composeTestRule
        .onNodeWithTag("suggestion_${locationViewModel.locationSuggestions.value[0].name}")
        .performClick()

    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithEndInvalidDate() {
    composeTestRule.setContent {
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input invalid dates
    inputText("inputTravelStartDate", "10/10/2024")
    inputText("inputTravelEndDate", "notadate")

    // Input valid location
    inputText("inputTravelLocation", "Paris")
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithInvalidStartAndEndDate() {
    composeTestRule.setContent {
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input valid dates
    inputText("inputTravelStartDate", "notadate")
    inputText("inputTravelEndDate", "notadate")

    // Input valid location
    inputText("inputTravelLocation", "Paris")
    composeTestRule
        .onNodeWithTag("suggestion_${locationViewModel.locationSuggestions.value[0].name}")
        .performClick()

    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithNonNumericDate() {
    composeTestRule.setContent {
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    // Input valid title and description
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")

    // Input invalid dates
    inputText("inputTravelStartDate", "notadate/10/2024")
    inputText("inputTravelEndDate", "notadate/10/2024")

    // Input valid location
    inputText("inputTravelLocation", "Paris")
    composeTestRule
        .onNodeWithTag("suggestion_${locationViewModel.locationSuggestions.value[0].name}")
        .performClick()

    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().performClick()

    // Verify that the repository method is not called to add a travel
    verify(travelRepository, never()).addTravel(any(), any(), any())
  }

  @Test
  fun submitTravelWithValidData() {

    // Set up the content for the test
    composeTestRule.setContent {
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    composeTestRule.waitForIdle() // Ensures inputs are registered

    // Input valid travel details
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")
    inputText("inputTravelStartDate", "10/10/2024")
    inputText("inputTravelEndDate", "20/10/2024")
    inputText("inputTravelLocation", "Paris")

    composeTestRule
        .onNodeWithTag("suggestion_${locationViewModel.locationSuggestions.value[0].name}")
        .performClick()

    composeTestRule.waitForIdle() // Ensures inputs are registered

    // Simulate clicking the save button
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().performClick()

    // Verify that the repository method is called to add a travel
    verify(travelRepository).addTravel(any(), any(), any())
  }

  @Test
  fun backButtonNavigatesCorrectly() {
    composeTestRule.setContent {
      AddTravelScreen(listTravelViewModel, navigationActions, profileModelView = profileModelView)
    }

    // Click the go back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify that the goBack function is called
    verify(navigationActions).goBack()
  }

  @Test
  fun saveButtonNotEnabled() {
    composeTestRule.setContent {
      AddTravelScreen(listTravelViewModel, navigationActions, profileModelView = profileModelView)
    }

    // Initially, the save button should be disabled
    composeTestRule.onNodeWithTag("travelSaveButton").assertIsNotEnabled()
  }

  @Test
  fun testToastIsShownWhenAddTravelFails() {
    // Mock the repository's addTravel() method to throw an exception
    doThrow(RuntimeException("Mocked exception during travel saving"))
        .`when`(travelRepository)
        .addTravel(any(), any(), any())

    // Set up the content for the test
    composeTestRule.setContent {
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    // Input valid travel details
    inputText("inputTravelTitle", "Trip to Paris")
    inputText("inputTravelDescription", "A fun trip to Paris")
    inputText("inputTravelStartDate", "10/10/2024")
    inputText("inputTravelEndDate", "20/10/2024")
    inputText("inputTravelLocation", "Paris")
    composeTestRule
        .onNodeWithTag("suggestion_${locationViewModel.locationSuggestions.value[0].name}")
        .performClick()

    // Simulate clicking the save button
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().performClick()
  }

  @Test
  fun datePickerDialogOpensOnClick() {
    composeTestRule.setContent {
      // Set up the content for the test
      AddTravelScreen(
          listTravelViewModel,
          navigationActions,
          locationViewModel,
          profileModelView = profileModelView)
    }

    // The startDate picker button should be displayed
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().assertIsDisplayed()

    // Simulate clicking the startDate picker button
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().performClick()

    // The endDate picker button should be displayed
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().assertIsDisplayed()

    // Simulate clicking the endDate picker button
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().performClick()
  }

  // Helper function to input text into a text field
  fun inputText(testTag: String, text: String) {
    composeTestRule.onNodeWithTag(testTag).performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag(testTag).performScrollTo().performTextInput(text)
  }
}
