package com.github.se.travelpouch.e2e

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.di.AppModule
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class)
class TravelCreation {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject lateinit var firestore: FirebaseFirestore

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @After
  fun tearDown() {
    runBlocking { firestore.terminate().await() }
  }

  @Test
  fun verifyUserFlowForTravelCreation() =
      runTest(timeout = 300.seconds) {

        // assert that login screen is displayed
        composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcomText").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in with email and password").assertIsDisplayed()

        // go to sign in screen with email and password and log in
        composeTestRule.onNodeWithText("Sign in with email and password").performClick()

        composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("travelpouchtest2@gmail.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("travelpouchtest2password")
        composeTestRule.onNodeWithText("Sign up").performClick()

        // Skip onboarding
        composeTestRule.waitUntil(timeoutMillis = 2000) {
          composeTestRule.onNodeWithTag("OnboardingScreen", useUnmergedTree = true).isDisplayed()
        }
        composeTestRule.onNodeWithTag("SkipButton").performClick()

        // wait until we are in the travel list screen
        composeTestRule.waitUntil(timeoutMillis = 2000) {
          composeTestRule.onNodeWithTag("emptyTravelPrompt", useUnmergedTree = true).isDisplayed()
        }

        // test that no travels are displayed because we have a new account
        composeTestRule
            .onNodeWithTag("emptyTravelPrompt", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("createTravelFab").performClick()

        // wait until we are in the screen to add a travel
        composeTestRule.waitUntil(timeoutMillis = 2000) {
          composeTestRule.onNodeWithTag("travelTitle", useUnmergedTree = true).isDisplayed()
        }

        // test that everything is displayed
        composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelTitle").assertTextEquals("Create a new travel")
        composeTestRule.onNodeWithTag("inputTravelTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelDescription").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelLocation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelStartDate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("inputTravelEndDate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelSaveButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelSaveButton").assertTextEquals("Save")

        // input fields to create a travel
        composeTestRule.onNodeWithTag("inputTravelTitle").performTextInput("e2e travel 1")
        composeTestRule
            .onNodeWithTag("inputTravelDescription")
            .performTextInput("test travel description")
        composeTestRule.onNodeWithTag("inputTravelLocation").performTextInput("L")

        // wait to have La paz displayed
        composeTestRule.waitUntil(timeoutMillis = 4000) {
          composeTestRule.onNodeWithText("La Paz, Bolivia").isDisplayed()
        }

        composeTestRule.onNodeWithText("La Paz, Bolivia").performClick()
        composeTestRule.onNodeWithTag("inputTravelStartDate").performTextInput("10/11/2024")
        composeTestRule.onNodeWithTag("inputTravelEndDate").performTextInput("20/11/2024")

        // save the travel and go back to the list of travels
        composeTestRule.onNodeWithTag("travelSaveButton").performClick()

        // verify that the previous buttons are still here
        composeTestRule
            .onNodeWithTag("TravelListScreen", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()

        // verify that the empty travel prompt does not exist since we saved a travel
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onNodeWithTag("emptyTravelPrompt", useUnmergedTree = true)
              .isNotDisplayed()
        }

        composeTestRule.onNodeWithText("e2e travel 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("e2e travel 1").assert(hasText("e2e travel 1"))
        composeTestRule.onNodeWithText("e2e travel 1").assert(hasText("test travel description"))
        composeTestRule.onNodeWithText("e2e travel 1").assert(hasText("La Paz, Bolivia"))
        composeTestRule.onNodeWithText("e2e travel 1").assert(hasText("10/11/2024"))
      }
}
