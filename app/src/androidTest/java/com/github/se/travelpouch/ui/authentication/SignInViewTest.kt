package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.model.profile.Profile
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class SignInViewTest {

  val mockNavigationActions = mock(NavigationActions::class.java)
  val travelRepository = mock(TravelRepository::class.java)
  val profileRepository = mock(ProfileRepository::class.java)

  val travelViewModel = ListTravelViewModel(travelRepository)
  val profileModelView = ProfileModelView(profileRepository)
  val mockFirebaseAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
  val mockFirebaseUser: FirebaseUser = mock(FirebaseUser::class.java)
  val mockTaskTokenResult: Task<GetTokenResult> = mock(Task::class.java) as Task<GetTokenResult>

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signInScreen_isDisplayed() {
    composeTestRule.setContent {
      SignInScreen(navigationActions = mockNavigationActions, profileModelView, travelViewModel)
    }
    // Scaffold
    composeTestRule.onNodeWithTag("loginScreenScaffold").assertIsDisplayed()

    // Column
    composeTestRule.onNodeWithTag("loginScreenColumn").assertIsDisplayed()

    // Image
    composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appLogo").assertContentDescriptionEquals("App Logo")

    // Text
    composeTestRule.onNodeWithTag("welcomText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcomText").assertTextEquals("Welcome")
  }

  @Test
  fun signInScreen_googleSignInButtonClick_triggersSignIn() {
    composeTestRule.setContent {
      SignInScreen(
          navigationActions = mockNavigationActions,
          profileModelView,
          travelViewModel,
          auth = mockFirebaseAuth)
    }
    composeTestRule.onNodeWithTag("loginButtonRow").performClick()
  }

  @Test
  fun signInWithWmailAndPasswordIsDisplayed() {
    composeTestRule.setContent {
      SignInScreen(navigationActions = mockNavigationActions, profileModelView, travelViewModel)
    }

    composeTestRule.onNodeWithText("Sign in with email and password").assertIsDisplayed()
  }

  @Test
  fun signInWhenAlreadyAuthenticated() {
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.getIdToken(true)).thenReturn(mockTaskTokenResult)
    `when`(mockTaskTokenResult.isComplete).thenReturn(true)
    `when`(travelRepository.initAfterLogin { anyOrNull<() -> Unit>() }).then { null }

    composeTestRule.setContent {
      SignInScreen(
          navigationActions = mockNavigationActions,
          profileModelView = profileModelView,
          travelViewModel = travelViewModel,
          auth = mockFirebaseAuth)
    }

    composeTestRule.waitForIdle()
    runBlocking {
      `when`(profileRepository.initAfterLogin { anyOrNull<(Profile) -> Unit>() }).then {
        val onSuccess = it.arguments[0] as (Profile) -> Unit
        val profile =
            Profile(
                fsUid = "qwertzuiopasdfghjklyxcvbnm12",
                email = "test@swent.ch",
                username = "test",
                friends = emptyList(),
                name = "name",
                userTravelList = emptyList())
        onSuccess(profile)
      }
    }
    verify(mockNavigationActions).navigateTo(Screen.TRAVEL_LIST)
  }

  @Test
  fun signInWhenAlreadyAuthenticatedError() {
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    doThrow(
            RuntimeException(
                "User account is disabled, deleted, or credentials are no longer valid"))
        .`when`(mockFirebaseUser)
        .getIdToken(true)
    `when`(mockTaskTokenResult.isComplete).thenReturn(true)
    `when`(travelRepository.initAfterLogin { anyOrNull<() -> Unit>() }).then { null }

    composeTestRule.setContent {
      SignInScreen(
          navigationActions = mockNavigationActions,
          profileModelView = profileModelView,
          travelViewModel = travelViewModel,
          auth = mockFirebaseAuth)
    }
    composeTestRule.waitForIdle()
    verify(mockNavigationActions, never()).navigateTo(Screen.TRAVEL_LIST)
  }
}
