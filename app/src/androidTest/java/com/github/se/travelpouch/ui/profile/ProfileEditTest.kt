// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.notifications.NotificationRepository
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.profile.ProfileRepositoryFirebase
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProfileEditTest {
  private lateinit var profileModelView: ProfileModelView
  private lateinit var navigationActions: NavigationActions
  private lateinit var profileRepository: ProfileRepository
  private lateinit var notificationViewModel: NotificationViewModel
  private lateinit var notificationRepository: NotificationRepository

  val profile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "test@swent.ch",
          username = "test",
          friends = mapOf("email@email.com" to "uid1"),
          name = "name",
          userTravelList = emptyList())

  val newProfile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "newtest@test.ch",
          username = "newUsername",
          friends = mapOf("email@email.com" to "uid1"),
          name = "newName",
          userTravelList = emptyList())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView(profileRepository)
    notificationRepository = mock(NotificationRepository::class.java)
    notificationViewModel = NotificationViewModel(notificationRepository)
  }

  @Test
  fun verifiesTheProfileIsCorrectlyUpdated() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    composeTestRule.setContent {
      ModifyingProfileScreen(navigationActions, profileModelView, notificationViewModel)
    }

    composeTestRule.onNodeWithTag("ProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertTextContains(profile.email)
    composeTestRule.onNodeWithTag("usernameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("usernameField").assertTextContains(profile.username)
    composeTestRule.onNodeWithTag("nameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nameField").assertTextContains(profile.name)

    composeTestRule.onNodeWithTag("usernameField").performTextClearance()
    composeTestRule.onNodeWithTag("usernameField").performTextInput(newProfile.username)
    composeTestRule.onNodeWithTag("nameField").performTextClearance()
    composeTestRule.onNodeWithTag("nameField").performTextInput(newProfile.name)

    composeTestRule.onNodeWithTag("usernameField").assertTextContains(newProfile.username)

    composeTestRule.onNodeWithTag("saveButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(profileRepository).updateProfile(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun addingAFriendWorks() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    composeTestRule.setContent {
      ModifyingProfileScreen(navigationActions, profileModelView, notificationViewModel)
    }

    composeTestRule.onNodeWithTag("floatingButtonAddingFriend").assertIsDisplayed()
    composeTestRule.onNodeWithTag("floatingButtonAddingFriend").assertTextContains("Add Friend")
    composeTestRule.onNodeWithTag("addingFriendIcon", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("floatingButtonAddingFriend").performClick()
    composeTestRule.onNodeWithTag("boxAddingFriend").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addingFriendTitle").assertTextContains("Adding a friend")
    composeTestRule.onNodeWithTag("addingFriendButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("addingFriendField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addingFriendField").performTextClearance()
    composeTestRule.onNodeWithTag("addingFriendField").performTextInput("email@email.com")
    composeTestRule.onNodeWithTag("addingFriendButton").performClick()

    verify(profileRepository, never()).sendFriendNotification(anyOrNull(), anyOrNull(), anyOrNull())

    composeTestRule.onNodeWithTag("addingFriendField").performTextClearance()
    composeTestRule.onNodeWithTag("addingFriendField").performTextInput("test@swent.ch")
    composeTestRule.onNodeWithTag("addingFriendButton").performClick()

    verify(profileRepository, never()).sendFriendNotification(anyOrNull(), anyOrNull(), anyOrNull())

    composeTestRule.onNodeWithTag("addingFriendField").performTextClearance()
    composeTestRule.onNodeWithTag("addingFriendField").performTextInput("final@answer.com")
    composeTestRule.onNodeWithTag("addingFriendButton").performClick()

    verify(profileRepository).sendFriendNotification(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun testThatSuccessMethodSendsNotification() {
    val mockDatabase: FirebaseFirestore = mock()
    val mockCollectionReference: CollectionReference = mock()
    val mockQuery: Query = mock()
    val taskQuerySnapshot: Task<QuerySnapshot> = mock()
    val mockQuerySnapshot: QuerySnapshot = mock()
    val mockDocumentSnapshot: DocumentSnapshot = mock()

    whenever(mockDatabase.collection(anyOrNull())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.whereEqualTo(eq("email"), anyOrNull())).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(taskQuerySnapshot)

    whenever(taskQuerySnapshot.isSuccessful).thenReturn(true)
    whenever(taskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
    whenever(taskQuerySnapshot.addOnSuccessListener(anyOrNull())).thenReturn(taskQuerySnapshot)
    whenever(taskQuerySnapshot.addOnFailureListener(anyOrNull())).thenReturn(taskQuerySnapshot)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    whenever(mockDocumentSnapshot.id).thenReturn("qwertzuiopasdfghjklyxcvbnm12")
    whenever(mockDocumentSnapshot.getString("username")).thenReturn("username")
    whenever(mockDocumentSnapshot.getString("email")).thenReturn("email@email.com")
    whenever(mockDocumentSnapshot.get("friends")).thenReturn(emptyMap<String, String>())
    whenever(mockDocumentSnapshot.get("listoftravellinked")).thenReturn(emptyList<String>())
    whenever(mockDocumentSnapshot.getString("name")).thenReturn("name")

    whenever(notificationRepository.getNewUid()).thenReturn("qwertzuiopasdfghjkly")

    val profileModelView = ProfileModelView(ProfileRepositoryFirebase(mockDatabase))

    composeTestRule.setContent {
      ModifyingProfileScreen(navigationActions, profileModelView, notificationViewModel)
    }

    composeTestRule.onNodeWithTag("floatingButtonAddingFriend").performClick()
    composeTestRule.onNodeWithTag("addingFriendField").performTextClearance()
    composeTestRule.onNodeWithTag("addingFriendField").performTextInput("email@email.com")
    composeTestRule.onNodeWithTag("addingFriendButton").performClick()

    val onCompleteListenerCaptor2 = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(taskQuerySnapshot).addOnSuccessListener(onCompleteListenerCaptor2.capture())

    // This line was generated thanks to Chat-GPT. This allows us to display the Toast even if the
    // piece of code is executed from the argument captor
    composeTestRule.runOnIdle { onCompleteListenerCaptor2.firstValue.onSuccess(mockQuerySnapshot) }

    verify(notificationRepository).addNotification(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun verifiesRemovingFriendWorks() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    composeTestRule.setContent {
      ModifyingProfileScreen(navigationActions, profileModelView, notificationViewModel)
    }

    composeTestRule.onNodeWithTag("friendCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friendCard0").performClick()

    composeTestRule.onNodeWithTag("boxDeletingFriend").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeletingFriendTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deletingFriendButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("deletingFriendButton").performClick()

    composeTestRule.onNodeWithTag("boxDeletingFriend").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("DeletingFriendTitle").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("deletingFriendButton").assertIsNotDisplayed()

    verify(profileRepository).removeFriend(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
  }
}
