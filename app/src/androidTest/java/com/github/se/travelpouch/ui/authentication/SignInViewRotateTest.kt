package com.github.se.travelpouch.ui.authentication

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.travelpouch.MainActivity
import org.junit.Rule
import org.junit.Test

class SignInViewRotationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun signInScreen_rotate() {
    // Rotate the screen to landscape
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    // Assert that the UI is displayed correctly after rotation
    composeTestRule.onNodeWithTag("loginScreenScaffold").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginScreenColumn").assertIsDisplayed()
  }
}
