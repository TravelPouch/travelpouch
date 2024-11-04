package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.travelpouch.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SignInViewTest {

  val mockNavigationActions = mock(NavigationActions::class.java)

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signInScreen_isDisplayed() {
    composeTestRule.setContent { SignInScreen(navigationActions = mockNavigationActions) }
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
    composeTestRule.setContent { SignInScreen(navigationActions = mockNavigationActions) }
    composeTestRule.onNodeWithTag("loginButtonRow").performClick()
  }
}
