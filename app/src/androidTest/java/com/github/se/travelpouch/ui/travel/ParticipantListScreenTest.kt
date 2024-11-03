package com.github.se.travelpouch.ui.travel

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.TravelRepository
import com.github.se.travelpouch.model.fsUid
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ParticipantListScreenTest {

  private val container = createContainer()
  private val participant1 =
      Profile(
          fsUid = container.allParticipants.keys.toList()[0].fsUid,
          name = "User One",
          userTravelList = listOf("travel1"),
          email = "user1@example.com",
          friends = null,
          username = "username")
  private val participant2 =
      Profile(
          fsUid = container.allParticipants.keys.toList()[1].fsUid,
          name = "User Two",
          userTravelList = listOf("travel2"),
          email = "user2@example.com",
          friends = null,
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
            participants)
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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    travelRepository = mock(TravelRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    listTravelViewModel = ListTravelViewModel(travelRepository)
  }

  @Test
  fun testemptyView() {
    composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }

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
  fun testNonEmptyViewChangeRole() {
    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // Set the content of the screen
    composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }
    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantRow").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantRow").onLast()
    first.assertExists().assertIsDisplayed()
    second.assertExists().assertIsDisplayed()
    first.performScrollTo().performClick()
    composeTestRule.onNodeWithTag("participantDialogBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantDialogEmail").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("participantDialogRole")
        .assertIsDisplayed()
        .assertTextContains("Role : PARTICIPANT")
    composeTestRule.onNodeWithTag("changeRoleButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("changeRoleButton").performClick()
    composeTestRule.onNodeWithTag("roleDialogBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleDialogTitle").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("roleDialogCurrentRole")
        .assertIsDisplayed()
        .assertTextContains("Current Role: PARTICIPANT")
    composeTestRule.onNodeWithTag("ownerButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("organizerButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ownerButton").performClick()
    composeTestRule.onNodeWithTag("participantDialogBox").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("roleDialogBox").assertIsNotDisplayed()
  }

  @Test
  fun testNonEmptyViewChangeRoleFailed() {
    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // Set the content of the screen
    composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }
    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantRow").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantRow").onLast()
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
    composeTestRule.onAllNodesWithTag("participantRow").onFirst().performScrollTo().performClick()
  }

  @Test
  fun testNonEmptyViewChangeRoleToSameRole() {
    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // Set the content of the screen
    composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }
    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantRow").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantRow").onLast()
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
    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // Set the content of the screen
    composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }
    // listTravelViewModel.addParticipant(participant1)
    // listTravelViewModel.addParticipant(participant2)

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantRow").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantRow").onLast()
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
    listTravelViewModel.selectTravel(container)

    // this hack was generated using Github Copilot
    val participants_field = listTravelViewModel::class.java.getDeclaredField("participants_")
    participants_field.isAccessible = true
    val participantFlow =
        participants_field.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
    participantFlow.value =
        mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

    // Set the content of the screen
    composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }

    // Check if all elements are displayed
    composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantListSettingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantColumn").assertIsDisplayed()

    val first = composeTestRule.onAllNodesWithTag("participantRow").onFirst()
    val second = composeTestRule.onAllNodesWithTag("participantRow").onLast()
    first.assertExists().assertIsDisplayed()
    second.assertExists().assertIsDisplayed()
    second.performScrollTo().performClick()
    composeTestRule
        .onNodeWithTag("participantDialogRole")
        .assertIsDisplayed()
        .assertTextContains("Role : OWNER")
    composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed().performClick()
    composeTestRule.onAllNodesWithTag("participantRow").onFirst().performScrollTo().performClick()
  }
}
