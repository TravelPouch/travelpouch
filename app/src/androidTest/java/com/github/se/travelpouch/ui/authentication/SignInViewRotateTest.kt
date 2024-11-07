package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.profile.ProfileRepository
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelRepository
import com.github.se.travelpouch.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SignInViewRotationTest {

  @get:Rule val composeTestRule = createComposeRule()

  val mockNavigationActions = mock(NavigationActions::class.java)
  val travelRepository = mock(TravelRepository::class.java)
  val profileRepository = mock(ProfileRepository::class.java)

  val travelViewModel = ListTravelViewModel(travelRepository)
  val profileModelView = ProfileModelView(profileRepository)

  @Test
  fun signInScreen_rotate() {
    val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    // Rotate the screen to landscape
    uiDevice.setOrientationLeft()

    // Wait for the UI to settle after rotation
    uiDevice.waitForIdle(3000) // Waits up to 3 seconds

    composeTestRule.setContent {
      SignInScreen(
          navigationActions = mockNavigationActions,
          profileModelView = profileModelView,
          travelViewModel = travelViewModel)
    }

    // Assert that the UI is displayed correctly after rotation
    composeTestRule.onNodeWithTag("loginScreenScaffold").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginScreenColumn").assertIsDisplayed()
  }
}
