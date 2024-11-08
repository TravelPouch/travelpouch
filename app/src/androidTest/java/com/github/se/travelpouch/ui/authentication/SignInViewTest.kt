package com.github.se.travelpouch.ui.authentication

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SignInViewTest {

  val mockNavigationActions = mock(NavigationActions::class.java)
  val travelRepository = mock(TravelRepository::class.java)
  val profileRepository = mock(ProfileRepository::class.java)

  val travelViewModel = ListTravelViewModel(travelRepository)
  val profileModelView = ProfileModelView(profileRepository)

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
    composeTestRule.onNodeWithTag("appLogo").assertWidthIsEqualTo(250.dp)
    composeTestRule.onNodeWithTag("appLogo").assertHeightIsEqualTo(250.dp)

    // Text
    composeTestRule.onNodeWithTag("welcomText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcomText").assertTextEquals("Welcome")
  }

  @Test
  fun signInScreen_googleSignInButtonClick_triggersSignIn() {
    composeTestRule.setContent {
      SignInScreen(navigationActions = mockNavigationActions, profileModelView, travelViewModel)
    }
    composeTestRule.onNodeWithTag("loginButtonRow").performClick()
  }

  @Test
  fun signInScreenTestSpinner() {
    val yesSpin: MutableState<Boolean> = mutableStateOf(true)
    composeTestRule.setContent {
      SignInScreen(
          navigationActions = mockNavigationActions, profileModelView, travelViewModel, yesSpin)
    }
    composeTestRule.waitForIdle()
  }
}
