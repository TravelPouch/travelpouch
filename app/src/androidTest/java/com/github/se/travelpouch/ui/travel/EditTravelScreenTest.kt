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
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Participant
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.model.travels.fsUid
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never

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
    locationRepository = mock(LocationRepository::class.java)
      locationViewModel = LocationViewModel(locationRepository)
  }

  @Test
  fun checkNoSelectedTravel() {
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }

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
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
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
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }

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
  fun addUserButtonFails() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
    composeTestRule.onNodeWithTag("addUserFab").performClick()

    // perform add user
    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")
    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserEmailField").assertTextContains("")

    val randomEmail = "random.email@example.org"
    inputText("addUserEmailField", "", randomEmail)
    // Now this is an invalid user that doesn't exist
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Unknown API Error"))
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()

    composeTestRule.onNodeWithTag("addUserButton").performClick()
    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository, never()).addNotification(anyOrNull())
  }

  @Test
  fun addUserButtonWithUserUid() {
    val travelContainer = createContainer()

    val profile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "username",
            "email@gmail.com",
            null,
            "name",
            emptyList())

    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
    composeTestRule.onNodeWithTag("addUserFab").performClick()

    // perform add user
    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")
    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserEmailField").assertTextContains("")

    val randomEmail = "random.email@example.org"
    inputText("addUserEmailField", "", randomEmail)

    doAnswer { invocation ->
          val email = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(fsUid?) -> Unit>(1)
          val customUserInfo =
              Profile(
                  fsUid = profileModelView.profile.value.fsUid,
                  name = "Custom User",
                  userTravelList = listOf("00000000000000000000"),
                  email = email,
                  username = "username",
                  friends = null)
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()

    // Mock the repository.updateTravel method to do nothing
    doNothing().`when`(travelRepository).updateTravel(any(), any(), any())
    doAnswer { "sigmasigmasigmasigm2" }.`when`(travelRepository).getNewUid()
    composeTestRule.onNodeWithTag("addUserButton").performClick()

    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository, never()).addNotification(anyOrNull())
  }


    @Test
  fun addUserButtonFailsIfUserAlreadyInTravel() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
    composeTestRule.onNodeWithTag("addUserFab").performClick()

    // perform add user
    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")
    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserEmailField").assertTextContains("")

    val randomEmail = "random.email@example.org"
    inputText("addUserEmailField", "", randomEmail)

    doAnswer { invocation ->
          val email = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(fsUid?) -> Unit>(1)
          val customUserInfo =
              Profile(
                  fsUid = travelContainer.listParticipant[1],
                  name = "Custom User",
                  userTravelList = listOf("00000000000000000000"),
                  email = email,
                  username = "username",
                  friends = null)
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()

    // Mock the repository.updateTravel method to do nothing
    doNothing().`when`(travelRepository).updateTravel(any(), any(), any())
    composeTestRule.onNodeWithTag("addUserButton").performClick()
    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository, never()).addNotification(anyOrNull())
  }

  @Test
  fun addUserButtonWorksIfValidUidAndUserNotInTravel() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
    composeTestRule.onNodeWithTag("addUserFab").performClick()

    // perform add user
    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")
    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserEmailField").assertTextContains("")

    val randomEmail = "random.email@example.org"
    inputText("addUserEmailField", "", randomEmail)

    doAnswer { invocation ->
          val email = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(fsUid?) -> Unit>(1)
          val customUserInfo =
              Profile(
                  fsUid = "qwertzuiopasdfghjklyxcvbnm12",
                  name = "Custom User",
                  userTravelList = listOf("00000000000000000000"),
                  email = email,
                  username = "username",
                  friends = null)
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()
    // Mock the repository.updateTravel method to do nothing
    doNothing().`when`(travelRepository).updateTravel(any(), any(), any())
    composeTestRule.onNodeWithTag("addUserButton").performClick()
    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())

    verify(notificationRepository).addNotification(anyOrNull())
  }

  @Test
  fun pressALotOfButtons() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }
    composeTestRule.onNodeWithTag("importEmailFab").performClick()
    composeTestRule.onNodeWithTag("addUserFab").performClick()

    // perform add user
    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")
    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()

    val randomEmail = "random.email@example.org"
    inputText("addUserEmailField", "", randomEmail)
    // Now this is an invalid user that doesn't exist
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("User not found"))
        }
        .`when`(travelRepository)
        .checkParticipantExists(any(), any(), any())
    composeTestRule.onNodeWithTag("addUserButton").performClick()

    // Now this is a valid user that had serialisation problems
    inputText("addUserEmailField", randomEmail, "newuser.email@example.org")
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Profile?) -> Unit>(1)
          // Call the onSuccess callback with null
          onSuccess(null)
        }
        .`when`(travelRepository)
        .checkParticipantExists(any(), any(), any())
    // Mock the repository.updateTravel method to do nothing
    doNothing().`when`(travelRepository).updateTravel(any(), any(), any())
    composeTestRule.onNodeWithTag("addUserButton").performClick()

    // Now this is a valid user that does exist
    doAnswer { invocation ->
          val email = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(Profile?) -> Unit>(1)
          val customUserInfo =
              Profile(
                  fsUid = "abcdefghijklmnopqrstuvwxyz12",
                  name = "Custom User",
                  userTravelList = listOf("00000000000000000000"),
                  email = email,
                  username = "username",
                  friends = null)
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo)
        }
        .`when`(travelRepository)
        .checkParticipantExists(any(), any(), any())
    // Mock the repository.updateTravel method to do nothing
    doNothing().`when`(travelRepository).updateTravel(any(), any(), any())
    composeTestRule.onNodeWithTag("addUserButton").performClick()

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
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }

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
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }
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
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun saveButtonPressed() {
    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }

    composeTestRule.onNodeWithTag("travelSaveButton").isDisplayed()
  }

  @Test
  fun datePickerDialogOpensOnClick() {

    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)

    composeTestRule.setContent {
      EditTravelSettingsScreen(
          listTravelViewModel, navigationActions, notificationViewModel, profileModelView, locationViewModel)
    }

    // The startDate picker button should be displayed
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().assertIsDisplayed()

    // Simulate clicking the date picker button
    composeTestRule.onNodeWithTag("startDatePickerButton").performScrollTo().performClick()

    // The endDate picker button should be displayed
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().assertIsDisplayed()

    // Simulate clicking the date picker button
    composeTestRule.onNodeWithTag("endDatePickerButton").performScrollTo().performClick()
  }
}
