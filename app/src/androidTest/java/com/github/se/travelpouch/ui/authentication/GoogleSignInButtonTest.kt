package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class GoogleSignInButtonTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun googleSignInButton_isDisplayed() {
    composeTestRule.setContent { GoogleSignInButton(onSignInClick = {}) }
    composeTestRule.onNodeWithTag("loginButtonRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertTextEquals("Sign in with Google")
    composeTestRule.onNodeWithTag("googleLogo").isDisplayed()
    composeTestRule
        .onNodeWithTag("googleLogo", useUnmergedTree = true)
        .assertContentDescriptionEquals("Google Logo")

    // Check if the row contains all the children
    composeTestRule.onNodeWithTag("loginButtonRow").onChildren().assertCountEquals(1)
    composeTestRule
        .onNodeWithTag("loginButtonRow")
        .onChildren()
        .assertAny(hasTestTag("loginButton"))
        .get(0)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("loginButton", useUnmergedTree = true)
        .onChildren()
        .assertCountEquals(2)
  }

  @Test
  fun googleSignInButton_sizeIsCorrect() {
    composeTestRule.setContent { GoogleSignInButton(onSignInClick = {}) }
    // Row
    composeTestRule.onNodeWithTag("loginButtonRow").assertHeightIsEqualTo(48.dp)
    composeTestRule.onNodeWithTag("loginButtonRow").assertWidthIsEqualTo(344.dp)

    // Button
    composeTestRule.onNodeWithTag("loginButton").assertHeightIsEqualTo(46.dp)
    composeTestRule.onNodeWithTag("loginButton").assertWidthIsEqualTo(344.dp)

    // Image
    composeTestRule.onNodeWithTag("googleLogo", useUnmergedTree = true).assertHeightIsEqualTo(30.dp)
    composeTestRule.onNodeWithTag("googleLogo", useUnmergedTree = true).assertWidthIsEqualTo(22.dp)
  }

  @Test
  fun googleSignInButton_clickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent { GoogleSignInButton(onSignInClick = { clicked = true }) }
    composeTestRule.onNodeWithTag("loginButton").performClick()
    assert(clicked)
  }
}
