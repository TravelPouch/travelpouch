package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.authentication.MockFirebaseAuthenticationService
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify

class TestSignInEmailPasswordUI {
  val mockNavigationActions = mock(NavigationActions::class.java)
  val travelRepository = mock(TravelRepository::class.java)
  val profileRepository = mock(ProfileRepository::class.java)

  val travelViewModel = ListTravelViewModel(travelRepository)
  val profileModelView = ProfileModelView(profileRepository)

  val authenticationService = MockFirebaseAuthenticationService()

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signingInWithPasswordWorks() =
      runTest(timeout = Duration.INFINITE) {
        composeTestRule.setContent {
          SignInWithPassword(
              mockNavigationActions, profileModelView, travelViewModel, authenticationService)
        }

        composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("travelpouchtest1@gmail.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("travelpouchtest1password")
        composeTestRule.onNodeWithText("Sign in").performClick()

        verify(profileRepository).initAfterLogin(anyOrNull())
        verify(mockNavigationActions).navigateTo(anyOrNull())
      }
}
