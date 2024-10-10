package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.travelpouch.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenAndroidTest : TestCase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test fun launchOnAuthComplete() {}

  @Test fun launchOnAuthError() {}

  @Test
  fun scaffoldEntityLabeledAreDisplay() {
    // Logo
    composeTestRule.onNodeWithTag("loginLogo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginLogo").assertContentDescriptionEquals("App Logo")
    composeTestRule.onNodeWithTag("loginLogo").assertWidthIsAtLeast(100.dp)
    composeTestRule.onNodeWithTag("loginLogo").assertTouchWidthIsEqualTo(250.dp)
    composeTestRule.onNodeWithTag("loginLogo").assertHeightIsAtLeast(100.dp)
    composeTestRule.onNodeWithTag("loginLogo").assertTouchHeightIsEqualTo(250.dp)

    // Title
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Welcome")

    // Google LogIn Button
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("loginButton").assertTextContains("Sign in with Google")
    composeTestRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sign in with Google").assertHasClickAction()
    composeTestRule
        .onNodeWithTag("googleLogo", useUnmergedTree = true)
        .assertContentDescriptionEquals("Google Logo")
    composeTestRule
        .onNodeWithTag("googleLogo", useUnmergedTree = true)
        .assertTouchWidthIsEqualTo(22.dp)
    composeTestRule
        .onNodeWithTag("googleLogo", useUnmergedTree = true)
        .assertTouchHeightIsEqualTo(30.dp)
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.onNodeWithTag("loginButton").performClick()
    composeTestRule.waitForIdle()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }

  @Test fun scaffoldGoogleSignInButton() {}

  @Test fun googleSignInButton() {}

  @Test fun rememberFirebaseAuthLauncherSuccess() {}

  @Test fun rememberFirebaseAuthLauncherError() {}
}
