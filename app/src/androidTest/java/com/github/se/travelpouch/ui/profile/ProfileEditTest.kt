package com.github.se.travelpouch.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ProfileEditTest {
  private lateinit var profileModelView: ProfileModelView
  private lateinit var navigationActions: NavigationActions
  private lateinit var profileRepository: ProfileRepository

  val profile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "test@swent.ch",
          username = "test",
          friends = listOf("email@email.com"),
          name = "name",
          userTravelList = emptyList())

  val newProfile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "newtest@test.ch",
          username = "newUsername",
          friends = emptyList(),
          name = "newName",
          userTravelList = emptyList())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView(profileRepository)
  }

  @Test
  fun verifiesTheProfileIsCorrectlyUpdated() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }
    profileModelView.getProfile()

    composeTestRule.setContent { ModifyingProfileScreen(navigationActions, profileModelView) }

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

    composeTestRule.setContent { ModifyingProfileScreen(navigationActions, profileModelView) }

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

    verify(profileRepository, never())
        .addFriend(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

    composeTestRule.onNodeWithTag("addingFriendField").performTextClearance()
    composeTestRule.onNodeWithTag("addingFriendField").performTextInput("test@swent.ch")
    composeTestRule.onNodeWithTag("addingFriendButton").performClick()

    verify(profileRepository, never())
        .addFriend(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

    composeTestRule.onNodeWithTag("addingFriendField").performTextClearance()
    composeTestRule.onNodeWithTag("addingFriendField").performTextInput("final@answer.com")
    composeTestRule.onNodeWithTag("addingFriendButton").performClick()

    verify(profileRepository)
        .addFriend(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
  }
}
