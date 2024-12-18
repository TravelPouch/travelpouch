// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.travel

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.location.LocationRepository
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.notifications.NotificationRepository
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doNothing

class EditTravelSettingsScreenTest {
  // Helper function to input text into a text field
  fun inputText(testTag: String, previousText: String, text: String) {
    composeTestRule.onNodeWithTag(testTag).performScrollTo()
    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed().assertTextContains(previousText)
    composeTestRule.onNodeWithTag(testTag).performTextClearance()
    composeTestRule.onNodeWithTag(testTag).performTextInput(text)
    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed().assertTextContains(text)
  }

  fun createContainer(): TravelContainer {
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Paris")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"
    val user1ID = "rythwEmprFhOOgsANXnv12345678"
    val user2ID = "sigmasigmasigmasigma12345678"
    val participants: MutableMap<Participant, Role> = HashMap()
    val listParticipant = listOf(user1ID, user2ID)
    participants[Participant(user1ID)] = Role.OWNER
    participants[Participant(user2ID)] = Role.PARTICIPANT
    val travelContainer =
        TravelContainer(
            "DFZft6Z95ABnRQ3YZQ2d",
            "Test Title",
            "Test Description",
            Timestamp(1234567890L + 3600, 0),
            Timestamp(1234567890L + 200_000L, 0),
            location,
            attachments,
            participants,
            listParticipant)
    return travelContainer
  }

  private lateinit var travelRepository: TravelRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel
  private lateinit var notificationViewModel: NotificationViewModel
  private lateinit var notificationRepository: NotificationRepository
  private lateinit var profileModelView: ProfileModelView
  private lateinit var profileRepository: ProfileRepository
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    travelRepository = mock(TravelRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    listTravelViewModel = ListTravelViewModel(travelRepository)
    notificationRepository = mock(NotificationRepository::class.java)
    notificationViewModel = NotificationViewModel(notificationRepository)
    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView(profileRepository)
    locationViewModel =
        LocationViewModel(
            com.github.se.travelpouch.ui.home.AddTravelScreenTest.FakeLocationRepository())
  }

  @Test
  fun checkNoSelectedTravel() {
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    composeTestRule.onNodeWithTag("editScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertTextEquals("Edit Travel")
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("plusButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("manageParticipantsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("importEmailFab").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("noTravelSelectedText")
        .assertIsDisplayed()
        .assertTextContains(
            "No Travel to be edited was selected. If you read this message an error has occurred.")
  }

  @Test
  fun checkSelectedTravel() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(
        travelContainer) // this causes strange overwrite, shouldn't happen IRL
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    composeTestRule.onNodeWithTag("editScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertTextEquals("Edit Travel")
    composeTestRule.onNodeWithTag("editTravelColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelParticipantIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputParticipants").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelStartTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelEndTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelDeleteButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("plusButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("manageParticipantsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("importEmailFab").assertIsDisplayed()
  }

  @Test
  fun testChangeInput() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    inputText("inputTravelTitle", travelContainer.title, "Test Title")
    inputText("inputTravelDescription", travelContainer.description, "Test Description")
    inputText("inputTravelLocation", travelContainer.location.name, "Paris")
    inputText("inputTravelStartTime", "14/02/2009", "14/02/2009")
    inputText("inputTravelEndTime", "16/02/2009", "15/02/2009")
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").assertTextContains("Save")
    composeTestRule.onNodeWithTag("travelDeleteButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelDeleteButton").assertTextContains("Delete")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    verify(travelRepository).updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
  }

  fun pressALotOfButtons() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }
    composeTestRule.onNodeWithTag("plusButton").performClick()
    composeTestRule.onNodeWithTag("manageParticipantsButton").performClick()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    composeTestRule.onNodeWithTag("plusButton").performClick()
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // perform deletion of travel
    composeTestRule.onNodeWithTag("travelDeleteButton").performClick()
    doNothing().`when`(navigationActions).goBack()
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    doNothing().`when`(navigationActions).goBack()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("editTravelParticipantIcon").performClick()
  }

  @Test
  fun testInput() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    inputText("inputTravelStartTime", "14/02/2009", "14/02/2009")
    inputText("inputTravelEndTime", "16/02/2009", "15/02/2009")
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").assertTextContains("Save")
    composeTestRule.onNodeWithTag("travelDeleteButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelDeleteButton").assertTextContains("Delete")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    verify(travelRepository, atLeastOnce())
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
  }

  @Test
  fun testbadInputs() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }
    // check bad dates
    inputText("inputTravelStartTime", "14/02/2009", "14/02/2009")
    inputText("inputTravelEndTime", "16/02/2009", "gyat")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    inputText("inputTravelEndTime", "gyat", "13/02/2009")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    inputText("inputTravelEndTime", "13/02/2009", "18/02/2009")
    // check bad locations

  }

  @Test
  fun backButtonNavigatesCorrectly() {
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun saveButtonPressed() {
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    composeTestRule.onNodeWithTag("travelSaveButton").isDisplayed()
  }

  @Test
  fun datePickerDialogOpensOnClick() {

    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)

    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().performClick()
  }

  @Test
  fun dropdownMenuOpensOnClick() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)

    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    // Click on the location dropdown menu
    composeTestRule.onNodeWithTag("inputTravelLocation").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLocation").performScrollTo().performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check if the dropdown menu is displayed when change location
    composeTestRule.onNodeWithTag("inputTravelLocation").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("inputTravelLocation").performTextInput("New Location")

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on the location dropdown menu
    composeTestRule.onNodeWithTag("locationDropdownMenu").assertIsDisplayed()
  }

  @Test
  fun locationDropdownAppearsAndSelectionWorks() {
    val testQuery = "Paris"
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          locationViewModel)
    }

    // Type in the location field
    composeTestRule.onNodeWithTag("inputTravelLocation").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLocation").performScrollTo().performTextClearance()
    composeTestRule
        .onNodeWithTag("inputTravelLocation")
        .performScrollTo()
        .performTextInput(testQuery)

    // The dropdown should appear with the suggestion
    composeTestRule
        .onNodeWithTag("suggestion_${locationViewModel.locationSuggestions.value[0].name}")
        .performScrollTo()
        .performClick()
  }
}
