package com.github.se.travelpouch

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testGreetingDisplayed() {
    // Start the app by setting the content of the composables
    composeTestRule.setContent { TravelPouchApp() }

    // Find the Greeting text by its test tag and check if it is displayed
    composeTestRule
        .onNodeWithTag("GreetingText") // Use the test tag from the Greeting Composable
        .assertIsDisplayed()
  }
}
