package com.github.se.travelpouch.ui.endToEnd

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.time.delay
import org.junit.Rule
import org.junit.Test

class EndToEndTest {

  @get:Rule var composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun endToEndTest() {

      composeTestRule.onNodeWithText("Sign in with Email and Password").assertIsDisplayed()
      composeTestRule.onNodeWithText("Sign in with Email and Password").performClick()

      composeTestRule.onNodeWithTag("emailSignIn").assertIsDisplayed()
      composeTestRule.onNodeWithTag("emailSignIn").performClick()

      composeTestRule.onNodeWithTag("emailSignIn").performTextInput("travelpouchswenttest2027@gmail.com")
      composeTestRule.onNodeWithTag("passwordSignIn").assertIsDisplayed()
      composeTestRule.onNodeWithTag("passwordSignIn").performClick()

      composeTestRule.onNodeWithTag("passwordSignIn").performTextInput("travelPouchSwentTest")

      composeTestRule.onNodeWithText("Create account and Log in").assertIsDisplayed()
      composeTestRule.onNodeWithText("Create account and Log in").performClick()

      //composeTestRule.waitUntilAtLeastOneExists(hasTestTag("emptyTravelPrompt"), 5000)

      composeTestRule.waitUntil {
        composeTestRule.onNodeWithTag("emptyTravelPrompt").isDisplayed()
      }
      composeTestRule.onNodeWithTag("emptyTravelPrompt").assertIsDisplayed()


    //
    //     composeTestRule.onNodeWithText("You have no travels yet.").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("createTravelFab").performClick()
    //        Thread.sleep(2000)
    //
    //        composeTestRule.onNodeWithText("Create a new travel").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("inputTravelTitle").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("end to end")
    //        composeTestRule.onNodeWithTag("inputTravelDescription").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("inputTravelDescription").performTextInput("end to end
    // description")
    //        composeTestRule.onNodeWithTag("inputTravelLocation").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("inputTravelLocation")
    //            .performTextInput("q")
    //        Thread.sleep(2000)
    //        composeTestRule.onNodeWithText("Neuquén, Argentina").assertIsDisplayed()
    //        composeTestRule.onNodeWithText("Neuquén, Argentina").performClick()
    //
    //        composeTestRule.onNodeWithTag("inputTravelStartDate").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("inputTravelStartDate")
    //            .performTextInput("12/11/2024")
    //        composeTestRule.onNodeWithTag("inputTravelEndDate").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("inputTravelEndDate")
    //            .performTextInput("14/11/2024")
    //
    //        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    //        composeTestRule.onNodeWithText("Save").performClick()

  }
}
