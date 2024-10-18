package com.github.se.travelpouch.ui.authentication

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
  fun googleSignInButton_clickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent { GoogleSignInButton(onSignInClick = { clicked = true }) }
    composeTestRule.onNodeWithTag("loginButton").performClick()
    assert(clicked)
  }
}
