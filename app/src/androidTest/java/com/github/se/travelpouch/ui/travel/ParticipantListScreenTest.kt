package com.github.se.travelpouch.ui.travel

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.se.travelpouch.model.profile.Profile
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

        // Mock participant data
        val participantsField = listTravelViewModel::class.java.getDeclaredField("participants_")
        participantsField.isAccessible = true
        val participantFlow =
            participantsField.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
        participantFlow.value =
            mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

        // Set the content of the screen
        composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }

        // Wait for the UI to settle
        composeTestRule.waitForIdle()

        // Ensure the participant list and top bar are displayed
        composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()

        // Check if the participants are rendered
        composeTestRule.onAllNodesWithTag("participantColumn").assertCountEquals(2)

        // Click on the first participant (PARTICIPANT) to open the dialog
        val firstParticipantRow = composeTestRule.onAllNodesWithTag("participantColumn").onFirst()
        firstParticipantRow.performScrollTo().performClick()

        // Verify the participant dialog is displayed with the role "PARTICIPANT"
        composeTestRule.onNodeWithTag("participantDialogBox").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("participantDialogRole")
            .assertIsDisplayed()
            .assertTextContains("Role : PARTICIPANT")

        // Ensure the change role button is present
        composeTestRule.onNodeWithTag("changeRoleButton").assertIsDisplayed()

        // Click the change role button
        composeTestRule.onNodeWithTag("changeRoleButton").performClick()

        // Verify the role dialog appears
        composeTestRule.onNodeWithTag("roleDialogBox").assertIsDisplayed()
        composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()

        // Verify current role is displayed correctly in the dialog
        composeTestRule
            .onNodeWithTag("roleDialogCurrentRole")
            .assertIsDisplayed()
            .assertTextContains("Current Role: PARTICIPANT")

        // Ensure the available role buttons are displayed
        composeTestRule.onNodeWithTag("ownerButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("organizerButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantButton").assertIsDisplayed()

        // Click the "Owner" button
        composeTestRule.onNodeWithTag("ownerButton").performClick()

        composeTestRule.onNodeWithTag("participantDialogBox").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("roleDialogBox").assertIsNotDisplayed()
    }

    @Test
    fun testNonEmptyViewChangeRoleFailed() {
        listTravelViewModel.selectTravel(container)

        // Mock participant data
        val participantsField = listTravelViewModel::class.java.getDeclaredField("participants_")
        participantsField.isAccessible = true
        val participantFlow =
            participantsField.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
        participantFlow.value =
            mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

        // Set the content of the screen
        composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }

        // Wait for the UI to settle
        composeTestRule.waitForIdle()

        // Ensure the participant list and top bar are displayed
        composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()

        // Check if the participants are rendered
        composeTestRule.onAllNodesWithTag("participantColumn").assertCountEquals(2)

        // Click on the second participant (OWNER) to open the dialog
        val secondParticipantRow = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
        secondParticipantRow.performScrollTo().performClick()

        // Verify the participant dialog is displayed with the role "OWNER"
        composeTestRule.onNodeWithTag("participantDialogBox").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("participantDialogRole")
            .assertIsDisplayed()
            .assertTextContains("Role : OWNER")

        // Ensure the change role button is present
        composeTestRule.onNodeWithTag("changeRoleButton").assertIsDisplayed()

        // Click the change role button
        composeTestRule.onNodeWithTag("changeRoleButton").performClick()

        // Verify the role dialog appears
        composeTestRule.onNodeWithTag("roleDialogBox").assertIsDisplayed()
        composeTestRule.onNodeWithTag("roleDialogColumn").assertIsDisplayed()

        // Verify current role is displayed correctly in the dialog
        composeTestRule
            .onNodeWithTag("roleDialogCurrentRole")
            .assertIsDisplayed()
            .assertTextContains("Current Role: OWNER")

        // Ensure the available role buttons are displayed
        composeTestRule.onNodeWithTag("ownerButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("organizerButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantButton").assertIsDisplayed()

        // Click the "Organizer" button
        composeTestRule.onNodeWithTag("organizerButton").performClick()

        // Ensure the participant dialog and role dialog are dismissed
        composeTestRule.onNodeWithTag("participantDialogBox").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("roleDialogBox").assertIsNotDisplayed()
    }

    @Test
    fun testNonEmptyViewChangeRoleToSameRole() {
        // Select the travel container
        listTravelViewModel.selectTravel(container)

        // Mock participant data
        val participantsField = listTravelViewModel::class.java.getDeclaredField("participants_")
        participantsField.isAccessible = true
        val participantFlow =
            participantsField.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
        participantFlow.value =
            mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

        // Set the content of the screen
        composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }

        // Wait for the UI to settle
        composeTestRule.waitForIdle()

        // Ensure the participant list is displayed
        composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()

        // Check if the participants are rendered
        composeTestRule.onAllNodesWithTag("participantColumn").assertCountEquals(2)

        // Click on the second participant (OWNER) to open the dialog
        val secondParticipantRow = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
        secondParticipantRow.performScrollTo().performClick()

        // Verify the participant dialog is displayed with the role "OWNER"
        composeTestRule
            .onNodeWithTag("participantDialogBox")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantDialogRole")
            .assertIsDisplayed()
            .assertTextContains("Role : OWNER")

        // Check that the change role button is present
        composeTestRule.onNodeWithTag("changeRoleButton")
            .assertIsDisplayed()

        // Click the change role button
        composeTestRule.onNodeWithTag("changeRoleButton").performClick()

        // Verify the role dialog appears
        composeTestRule.onNodeWithTag("roleDialogBox")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("roleDialogColumn")
            .assertIsDisplayed()

        // Verify current role is displayed correctly in the dialog
        composeTestRule
            .onNodeWithTag("roleDialogCurrentRole")
            .assertIsDisplayed()
            .assertTextContains("Current Role: OWNER")

        // Ensure the "Owner" role button is displayed and click it
        composeTestRule.onNodeWithTag("ownerButton")
            .assertIsDisplayed()
            .performClick()
    }


    @Test
    fun testNonEmptyViewRemoveParticipant() {
        // Select the travel container
        listTravelViewModel.selectTravel(container)

        // Prepare the participants data
        val participantsField = listTravelViewModel::class.java.getDeclaredField("participants_")
        participantsField.isAccessible = true
        val participantFlow =
            participantsField.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>

        // Mock participant data
        participantFlow.value = mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

        // Set the content of the screen
        composeTestRule.setContent {
            ParticipantListScreen(listTravelViewModel, navigationActions)
        }

        // Wait for UI to settle after setting the content
        composeTestRule.waitForIdle()

        // Ensure the participant list is displayed
        composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()

        // Verify that the participant rows are rendered correctly
        val participantRows = composeTestRule.onAllNodesWithTag("participantColumn")
        participantRows.assertCountEquals(2)  // Ensure two rows are rendered

        // Interact with the first participant row
        val firstParticipantRow = participantRows.onFirst()
        firstParticipantRow.assertExists().assertIsDisplayed()
        firstParticipantRow.performScrollTo().performClick()

        // Verify that the participant details dialog appears
        composeTestRule.onNodeWithTag("participantDialogRole")
            .assertIsDisplayed()
            .assertTextContains("Role : PARTICIPANT")

        // Click the remove participant button
        composeTestRule.onNodeWithTag("removeParticipantButton").assertIsDisplayed().performClick()

        // You should also assert that a Toast or UI update happens (if required)
        // For example, verify a Toast message appears or that the participant is removed
    }


    @Test
    fun testNonEmptyViewRemoveParticipantFail() {
        // Select the travel container
        listTravelViewModel.selectTravel(container)

        // Mock participant data
        val participantsField = listTravelViewModel::class.java.getDeclaredField("participants_")
        participantsField.isAccessible = true
        val participantFlow =
            participantsField.get(listTravelViewModel) as MutableStateFlow<Map<fsUid, Profile>>
        participantFlow.value =
            mapOf(participant1.fsUid to participant1, participant2.fsUid to participant2)

        // Set the content of the screen
        composeTestRule.setContent { ParticipantListScreen(listTravelViewModel, navigationActions) }

        // Wait for the UI to settle
        composeTestRule.waitForIdle()

        // Ensure the participant list is displayed
        composeTestRule.onNodeWithTag("participantListScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("participantListSettingTopBar").assertIsDisplayed()

        // Check if the participants are rendered
        composeTestRule.onAllNodesWithTag("participantColumn").assertCountEquals(2)

        // Click on the second participant (OWNER) to open the dialog
        val secondParticipantRow = composeTestRule.onAllNodesWithTag("participantColumn").onLast()
        secondParticipantRow.performScrollTo().performClick()

        // Verify the participant dialog is displayed with the role "OWNER"
        composeTestRule
            .onNodeWithTag("participantDialogRole")
            .assertIsDisplayed()
            .assertTextContains("Role : OWNER")

        // Try to remove the "OWNER" participant
        composeTestRule.onNodeWithTag("removeParticipantButton")
            .assertIsDisplayed()
            .performClick()

        // Wait for the UI to update after the removal attempt
        composeTestRule.waitForIdle()

        // After the removal attempt, ensure the "OWNER" is still in the list
        composeTestRule.onAllNodesWithTag("participantColumn").assertCountEquals(2)
    }



}
