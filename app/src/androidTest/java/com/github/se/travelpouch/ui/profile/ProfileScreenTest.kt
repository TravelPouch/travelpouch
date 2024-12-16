// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

class ProfileScreenTest {

  private lateinit var profileModelView: ProfileModelView
  private lateinit var navigationActions: NavigationActions
  private lateinit var profileRepository: ProfileRepository

  val profile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "test@swent.ch",
          username = "test",
          friends = mapOf("friend 1" to "uid1", "friend 2" to "uid2"),
          name = "testName",
          userTravelList = emptyList())

  val profileWithoutFriend =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "test@swent.ch",
          username = "test",
          friends = emptyMap(),
          name = "testName",
          userTravelList = emptyList())

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    profileRepository = mock(ProfileRepository::class.java)
    profileModelView = ProfileModelView(profileRepository)
  }

  @Test
  fun verifiesEverythingDisplayed() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }

    composeTestRule.setContent { ProfileScreen(navigationActions, profileModelView) }

    composeTestRule.onNodeWithTag("ProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertTextContains(profile.email)
    composeTestRule.onNodeWithTag("usernameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("usernameField").assertTextContains(profile.username)
    composeTestRule.onNodeWithTag("nameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nameField").assertTextContains(profile.name)

    composeTestRule.onNodeWithTag("friendsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friendsText").assertTextEquals("Friends : ")
  }

  @Test
  fun verifiesListOfFriendIsDisplayedIfFriendsNonEmpty() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profile)
    }

    composeTestRule.setContent { ProfileScreen(navigationActions, profileModelView) }

    composeTestRule.onNodeWithTag("friendCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friend_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friend_0").assertTextEquals("friend 1")

    composeTestRule.onNodeWithTag("friendCard1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friend_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friend_1").assertTextEquals("friend 2")
  }

  @Test
  fun verifiesEmptyTextIfNoFriends() {
    `when`(profileRepository.getProfileElements(anyOrNull(), anyOrNull())).then {
      it.getArgument<(Profile) -> Unit>(0)(profileWithoutFriend)
    }

    composeTestRule.setContent { ProfileScreen(navigationActions, profileModelView) }

    composeTestRule.onNodeWithTag("emptyFriendCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emptyFriendText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emptyFriendText").assertTextEquals("No friends are saved")
  }
}
