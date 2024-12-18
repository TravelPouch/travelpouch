// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.travel

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.events.EventRepository
import com.github.se.travelpouch.model.events.EventViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never

class ParticipantListScreenTest {

  private val container = createContainer()
  private val participant1 =
      Profile(
          fsUid = container.allParticipants.keys.toList()[0].fsUid,
          name = "User One",
          userTravelList = listOf("travel1"),
          email = "user1@example.com",
          friends = emptyMap(),
          username = "username")
  private val participant2 =
      Profile(
          fsUid = container.allParticipants.keys.toList()[1].fsUid,
          name = "User Two",
          userTravelList = listOf("travel2"),
          email = "user2@example.com",
          friends = emptyMap(),
          username = "username")

  private fun createContainer(): TravelContainer {
    val location = Location(12.34, 56.78, Timestamp(1234567890L, 0), "Test Location")
    val attachments: MutableMap<String, String> = HashMap()
    attachments["Attachment1"] = "UID1"
    val user1ID = "rythwEmprFhOOgsANXnvAAAAAAAA"
    val user2ID = "sigmasigmasigmasigmaAAAAAAAA"
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
            participants,
            listParticipant = emptyList())
    return travelContainer
  }

  fun generateAutoId(): String {
    // follows the format specified by random uid generator of firestore
    // https://github.com/firebase/firebase-android-sdk/blob/a024da69daa0a5264b133d9550d1cf068ec2b3ee/firebase-firestore/src/main/java/com/google/firebase/firestore/util/Util.java#L35
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..20).map { chars.random() }.joinToString("")
  }

  private lateinit var travelRepository: TravelRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listTravelViewModel: ListTravelViewModel
  private lateinit var notificationViewModel: NotificationViewModel
  private lateinit var notificationRepository: NotificationRepository
  private lateinit var profileModelView: ProfileModelView
  private lateinit var profileRepository: ProfileRepository
  private lateinit var eventRepository: EventRepository
  private lateinit var eventViewModel: EventViewModel

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
    eventRepository = mock(EventRepository::class.java)
    eventViewModel = EventViewModel(eventRepository)
  }

  @Test
  fun testemptyView() {
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("participantListSettingText")
        .assertIsDisplayed()
        .assertTextContains("Participant Settings")
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("noTravelSelected")
        .assertIsDisplayed()
        .assertTextContains("No Travel is selected or the selected travel no longer exists.")
    composeTestRule.onNodeWithTag("goBackButton").performClick()
  }

  @Test
  fun testNonEmptyViewChangeRoleFailed() {
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }

    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("callParticipantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantColumn").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
    first.assertExists().assertIsDisplayed()
    second.assertExists().assertIsDisplayed()
    second.performScrollTo().performClick()
    composeTestRule.onNodeWithTag("participantDialogBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogEmail").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("participantDialogRole")
        .assertIsDisplayed()
        .assertTextContains("Role : OWNER")
    composeTestRule.onNodeWithTag("changeRoleButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("changeRoleButton").performClick()
    composeTestRule.onNodeWithTag("roleDialogBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleDialogTitle").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("roleDialogCurrentRole")
        .assertIsDisplayed()
        .assertTextContains("Current Role: OWNER")
    composeTestRule.onNodeWithTag("ownerButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("organizerButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("organizerButton").performClick()
    composeTestRule.onNodeWithTag("participantDialogBox").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("roleDialogBox").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("participantColumn")
        .onFirst()
        .performScrollTo()
        .performClick()
  }

  @Test
  fun testNonEmptyViewChangeRoleToSameRole() {
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }

    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("callParticipantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantColumn").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
    first.assertExists().assertIsDisplayed()
    second.assertExists().assertIsDisplayed()
    second.performScrollTo().performClick()
    composeTestRule.onNodeWithTag("participantDialogBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogEmail").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("participantDialogRole")
        .assertIsDisplayed()
        .assertTextContains("Role : OWNER")
    composeTestRule.onNodeWithTag("changeRoleButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("changeRoleButton").performClick()
    composeTestRule.onNodeWithTag("roleDialogBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleDialogTitle").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("roleDialogCurrentRole")
        .assertIsDisplayed()
        .assertTextContains("Current Role: OWNER")
    composeTestRule.onNodeWithTag("ownerButton").assertIsDisplayed().performClick()
  }

  @Test
  fun testNonEmptyViewRemoveParticipant() {
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }

    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("callParticipantColumn").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("participantColumn").assertCountEquals(2)

    val first = composeTestRule.onAllNodesWithTag("participantColumn").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
    first.assertExists().assertIsDisplayed()
    second.assertExists().assertIsDisplayed()
    first.performScrollTo().performClick()
    composeTestRule
        .onNodeWithTag("participantDialogRole")
        .assertIsDisplayed()
        .assertTextContains("Role : PARTICIPANT")
    composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed().performClick()
  }

  @Test
  fun testNonEmptyViewRemoveParticipantFail() {
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }

    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("callParticipantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantColumn").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
    first.assertExists().assertIsDisplayed()
    second.assertExists().assertIsDisplayed()
    second.performScrollTo().performClick()
    composeTestRule
        .onNodeWithTag("participantDialogRole")
        .assertIsDisplayed()
        .assertTextContains("Role : OWNER")
    composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed().performClick()
    composeTestRule
        .onAllNodesWithTag("participantColumn")
        .onFirst()
        .performScrollTo()
        .performClick()
  }

  @Test
  fun addUserButtonWithNullUid() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }

    // Open the Add User dialog
    composeTestRule.onNodeWithTag("addUserFab").performClick()

    // Assert that the dialog is displayed
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()

    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")

    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserEmailField").assertTextContains("")

    // Input random email
    val randomEmail = "random.email@example.org"
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)

    // Mock the repository behavior
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Profile?) -> Unit>(1)
          // Call the onSuccess callback with null
          onSuccess(null)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()

    // Mock the repository.updateTravel method to do nothing
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())

    // Click the Add User button
    composeTestRule.onNodeWithTag("addUserButton").performClick()

    // Verify interactions with the repositories
    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository, never()).addNotification(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun addUserButtonFails() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
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
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)

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
    verify(notificationRepository, never()).addNotification(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun addUserButtonWithUserUid() {
    val travelContainer = createContainer()

    val profile =
        Profile(
            "qwertzuiopasdfghjklyxcvbnm12",
            "username",
            "email@gmail.com",
            emptyMap(),
            "name",
            emptyList())

    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
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
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)

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
                  friends = emptyMap())
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()

    // Mock the repository.updateTravel method to do nothing
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
    doAnswer { "sigmasigmasigmasigm2" }.`when`(travelRepository).getNewUid()
    composeTestRule.onNodeWithTag("addUserButton").performClick()

    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository, never()).addNotification(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun addUserButtonFailsIfUserAlreadyInTravel() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
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
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)

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
                  friends = emptyMap())
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()

    // Mock the repository.updateTravel method to do nothing
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
    composeTestRule.onNodeWithTag("addUserButton").performClick()
    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun addUserButtonWorksIfValidUidAndUserNotInTravel() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
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
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)

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
                  friends = emptyMap())
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())

    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()
    // Mock the repository.updateTravel method to do nothing
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
    composeTestRule.onNodeWithTag("addUserButton").performClick()
    verify(profileRepository).getFsUidByEmail(anyOrNull(), anyOrNull(), anyOrNull())
    verify(notificationRepository, atLeastOnce())
        .addNotification(anyOrNull(), anyOrNull(), anyOrNull())
    // throw impossible exception
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
                  friends = emptyMap())
          // Call the onSuccess callback with the custom UserInfo

          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())
    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
    doThrow(RuntimeException("Impossible Exception"))
        .`when`(notificationRepository)
        .addNotification(anyOrNull(), anyOrNull(), anyOrNull())
    composeTestRule.onNodeWithTag("addUserFab").performClick()
    val randomEmail2 = "random.email@example.org"
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail2)
    composeTestRule.onNodeWithTag("addUserButton").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun addFriendMenuWithNoFriends() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
    composeTestRule.onNodeWithTag("addUserFab").performClick()
    composeTestRule.onNodeWithTag("addViaFriendListButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("friendListDialogBox", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("friendListDialogTitle", useUnmergedTree = true)
        .assertTextContains("Select a Friend")
    composeTestRule
        .onNodeWithTag("noFriendsDialogText", useUnmergedTree = true)
        .assertTextContains("No friends to choose from")
  }

  @Test
  fun addFriendMenuWithFriend() {
    val travelContainer = createContainer()
    listTravelViewModel.selectTravel(travelContainer)

    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(
          Profile(
              fsUid = "abcdefghijklmnopqrstuvwxyz13",
              name = "Custom User",
              userTravelList = listOf("00000000000000000000"),
              email = "email@email.org",
              username = "username",
              friends = mapOf("example@mail.com" to "abcdefghijklmnopqrstuvwxyz12")))
    }
    profileModelView.getProfile()

    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
    composeTestRule.onNodeWithTag("addUserFab").performClick()
    composeTestRule.onNodeWithTag("addViaFriendListButton").performClick()
    composeTestRule.onNodeWithTag("friendListDialogBox", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("friendListDialogTitle", useUnmergedTree = true)
        .assertTextContains("Select a Friend")
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
                  friends = emptyMap())
          // Call the onSuccess callback with the custom UserInfo

          onSuccess(customUserInfo.fsUid)
        }
        .`when`(profileRepository)
        .getFsUidByEmail(any(), any(), any())
    doAnswer { "abcdefghijklmnopqrst" }.`when`(notificationRepository).getNewUid()
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
    composeTestRule.onNodeWithTag("friendCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friendCard").assertTextContains("example@mail.com")
    composeTestRule.onNodeWithTag("friendCard").performClick()
    verify(notificationRepository).addNotification(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun addUserALotOfButton() {
    composeTestRule.setContent {
      ParticipantListScreen(
          listTravelViewModel,
          navigationActions,
          notificationViewModel,
          profileModelView,
          eventViewModel)
    }
    // perform add user
    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("addUserFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    // Check that the title text is displayed and correct
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserDialogTitle").assertTextEquals("Add User by Email")
    // Check that the OutlinedTextField is displayed and has the correct default value
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed()

    val randomEmail = "random.email@example.org"
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule.onNodeWithTag("addUserEmailField").assertIsDisplayed().assertTextContains("")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)
    // Now this is an invalid user that doesn't exist
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("User not found"))
        }
        .`when`(travelRepository)
        .checkParticipantExists(any(), any(), any())
    composeTestRule.onNodeWithTag("addUserButton").performClick()

    // Now this is a valid user that had serialisation problems
    composeTestRule.onNodeWithTag("addUserEmailField").performScrollTo()
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains("random.email@example.org")
    composeTestRule.onNodeWithTag("addUserEmailField").performTextClearance()
    composeTestRule.onNodeWithTag("addUserEmailField").performTextInput(randomEmail)
    composeTestRule
        .onNodeWithTag("addUserEmailField")
        .assertIsDisplayed()
        .assertTextContains(randomEmail)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Profile?) -> Unit>(1)
          // Call the onSuccess callback with null
          onSuccess(null)
        }
        .`when`(travelRepository)
        .checkParticipantExists(any(), any(), any())
    // Mock the repository.updateTravel method to do nothing
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
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
                  friends = emptyMap())
          // Call the onSuccess callback with the custom UserInfo
          onSuccess(customUserInfo)
        }
        .`when`(travelRepository)
        .checkParticipantExists(any(), any(), any())
    // Mock the repository.updateTravel method to do nothing
    doNothing()
        .`when`(travelRepository)
        .updateTravel(any(), any(), anyOrNull(), any(), any(), anyOrNull())
    composeTestRule.onNodeWithTag("addUserButton").performClick()
  }
}
