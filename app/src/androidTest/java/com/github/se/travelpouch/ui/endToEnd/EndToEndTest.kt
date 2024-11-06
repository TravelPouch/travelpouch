package com.github.se.travelpouch.ui.endToEnd

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.TravelPouchApp
import com.github.se.travelpouch.ui.theme.SampleAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.junit.Rule
import org.junit.Test

class EndToEndTest {

  @get:Rule var composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun endToEndTest() {

    Log.d("USER", "${Firebase.auth.currentUser!!.email}")

    composeTestRule.setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().testTag("MainScreenContainer"),
        ) {
          TravelPouchApp()
        }
      }
    }

    //        composeTestRule.onNodeWithText("Sign in with Email and Password").assertIsDisplayed()
    //        composeTestRule.onNodeWithText("Sign in with Email and Password").performClick()
    //        Thread.sleep(10000)
    //        composeTestRule.onNodeWithTag("emailSignIn").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("emailSignIn").performClick()
    //
    // composeTestRule.onNodeWithTag("emailSignIn").performTextInput("travelpouchswenttest@gmail.com")
    //        composeTestRule.onNodeWithTag("passwordSignIn").assertIsDisplayed()
    //        composeTestRule.onNodeWithTag("passwordSignIn").performClick()
    //
    // composeTestRule.onNodeWithTag("passwordSignIn").performTextInput("travelPouchSwentTest")
    //
    //        composeTestRule.onNodeWithText("Create account and Log in").assertIsDisplayed()
    //        composeTestRule.onNodeWithText("Create account and Log in").performClick()
    //        Thread.sleep(5000)
    //
    //        composeTestRule.onNodeWithText("You have no travels yet.").assertIsDisplayed()
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
