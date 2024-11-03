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
          friends = null,
          name = "name",
          userTravelList = emptyList())

  val newProfile =
      Profile(
          fsUid = "qwertzuiopasdfghjklyxcvbnm12",
          email = "newtest@test.ch",
          username = "newUsername",
          friends = null,
          name = "name",
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
    composeTestRule.onNodeWithTag("friendsField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("friendsField").assertTextContains("No Friend, sadge :(")

    composeTestRule.onNodeWithTag("emailField").performTextClearance()
    composeTestRule.onNodeWithTag("emailField").performTextInput(newProfile.email)
    composeTestRule.onNodeWithTag("usernameField").performTextClearance()
    composeTestRule.onNodeWithTag("usernameField").performTextInput(newProfile.username)
    composeTestRule.onNodeWithTag("friendsField").assertTextContains("No Friend, sadge :(")

    composeTestRule.onNodeWithTag("emailField").assertTextContains(newProfile.email)
    composeTestRule.onNodeWithTag("usernameField").assertTextContains(newProfile.username)

    composeTestRule.onNodeWithTag("saveButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(profileRepository).updateProfile(anyOrNull(), anyOrNull(), anyOrNull())
  }
}
