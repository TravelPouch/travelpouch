package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.model.authentication.AuthenticationService
import com.github.se.travelpouch.model.authentication.MockFirebaseAuthenticationService
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import kotlin.time.Duration.Companion.seconds
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

  val authenticationService: AuthenticationService = mock()
  val authenticationServiceMock = MockFirebaseAuthenticationService()

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun verifiesTopBarAppDisplayed() {
    composeTestRule.setContent {
      SignInWithPassword(
          mockNavigationActions, profileModelView, travelViewModel, authenticationService)
    }

    composeTestRule.onNodeWithTag("PasswordTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PasswordTitle").assertTextEquals("Signing in with password")

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(mockNavigationActions).navigateTo(anyOrNull())
  }

  @Test
  fun signingInWithPasswordCallsCreateUser() =
      runTest(timeout = 20.seconds) {
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

        verify(authenticationService).createUser(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
      }

  @Test
  fun logInWithPasswordCallsLogin() =
      runTest(timeout = 20.seconds) {
        composeTestRule.setContent {
          SignInWithPassword(
              mockNavigationActions, profileModelView, travelViewModel, authenticationService)
        }

        composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("travelpouchtest1@gmail.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("travelpouchtest1password")
        composeTestRule.onNodeWithText("Log in").performClick()

        verify(authenticationService).login(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
      }

  @Test
  fun logInWithPasswordCallsProfileVM() =
      runTest(timeout = 20.seconds) {
        composeTestRule.setContent {
          SignInWithPassword(
              mockNavigationActions, profileModelView, travelViewModel, authenticationServiceMock)
        }

        composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("travelpouchtest1@gmail.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("travelpouchtest1password")
        composeTestRule.onNodeWithText("Log in").performClick()

        verify(profileModelView).initAfterLogin(anyOrNull())
        verify(mockNavigationActions).navigateTo(anyOrNull())
      }
}
