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
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
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
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"
    val user1ID = "rythwEmprFhOOgsANXnv"
    val user2ID = "sigmasigmasigmasigma"
    val participants: MutableMap<Participant, Role> = HashMap()
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
            participants)
    return travelContainer
  }

  private lateinit var travelRepository: TravelRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    travelRepository = mock(TravelRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    listTravelViewModel = ListTravelViewModel(travelRepository)
  }

  @Test
  fun checkNoSelectedTravel() {
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("editScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertTextEquals("Edit Travel")
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserFab").assertIsDisplayed()
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
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("editScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelText").assertTextEquals("Edit Travel")
    composeTestRule.onNodeWithTag("editTravelColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editTravelParticipantIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputParticipants").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLocationName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLatitude").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelLongitude").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelStartTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTravelEndTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelDeleteButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("importEmailFab").assertIsDisplayed()
  }

  @Test
  fun testChangeInput() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }

    inputText("inputTravelTitle", travelContainer.title, "Test Title")
    inputText("inputTravelDescription", travelContainer.description, "Test Description")
    inputText("inputTravelLocationName", travelContainer.location.name, "Test Location")
    inputText("inputTravelLatitude", travelContainer.location.latitude.toString(), "12.34")
    inputText("inputTravelLongitude", travelContainer.location.longitude.toString(), "56.78")
    inputText("inputTravelStartTime", "14/02/2009", "14/02/2009")
    inputText("inputTravelEndTime", "16/02/2009", "15/02/2009")
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").assertTextContains("Save")
    composeTestRule.onNodeWithTag("travelDeleteButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelDeleteButton").assertTextContains("Delete")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    verify(travelRepository).updateTravel(any(), any(), any())
  }

  @Test
  fun pressALotOfButtons() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }
    composeTestRule.onNodeWithTag("addUserFab").performClick()
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
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
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }

    inputText("inputTravelStartTime", "14/02/2009", "14/02/2009")
    inputText("inputTravelEndTime", "16/02/2009", "15/02/2009")
    composeTestRule.onNodeWithTag("travelSaveButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelSaveButton").assertTextContains("Save")
    composeTestRule.onNodeWithTag("travelDeleteButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("travelDeleteButton").assertTextContains("Delete")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    verify(travelRepository, atLeastOnce()).updateTravel(any(), any(), any())
  }

  @Test
  fun testbadInputs() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }
    // check bad dates
    inputText("inputTravelStartTime", "14/02/2009", "14/02/2009")
    inputText("inputTravelEndTime", "16/02/2009", "gyat")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    inputText("inputTravelEndTime", "gyat", "13/02/2009")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
    inputText("inputTravelEndTime", "13/02/2009", "18/02/2009")
    // check bad locations
    inputText("inputTravelLatitude", travelContainer.location.latitude.toString(), "gyat")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()

    inputText("inputTravelLatitude", "gyat", travelContainer.location.latitude.toString())
    inputText("inputTravelLongitude", travelContainer.location.longitude.toString(), "-200")
    composeTestRule.onNodeWithTag("travelSaveButton").performClick()
  }

  @Test
  fun backButtonNavigatesCorrectly() {
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun saveButtonPressed() {
    composeTestRule.setContent { EditTravelSettingsScreen(listTravelViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("travelSaveButton").isDisplayed()
  }
}
