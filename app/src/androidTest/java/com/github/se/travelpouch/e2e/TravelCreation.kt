package com.github.se.travelpouch.e2e

import androidx.compose.ui.test.ExperimentalTestApi
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
import androidx.test.espresso.intent.rule.IntentsTestRule
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.di.AppModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
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

  private val DEFAULT_TIMEOUT = 10000L

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @OptIn(ExperimentalTestApi::class)
  @get:Rule(order = 1)
  val composeTestRule =
      createAndroidComposeRule<MainActivity>(effectContext = Dispatchers.Main.immediate)

  @get:Rule(order = 2) val intentsTestRule = IntentsTestRule(MainActivity::class.java)

  @Inject lateinit var firestore: FirebaseFirestore
  @Inject lateinit var auth: FirebaseAuth

  @Before
  fun setUp() {
    hiltRule.inject()

    // seed DB with existing trave @Inject lateinit var auth: FirebaseAuthl and user
    runBlocking {
      val uid =
          auth
              .createUserWithEmailAndPassword("example1@example.com", "password1")
              .await()
              .user!!
              .uid

      firestore
          .collection("userslist")
          .document(uid)
          .set(
              mapOf(
                  "email" to "example1@example.com",
                  "friends" to emptyList<String>(),
                  "fsUid" to uid,
                  "name" to "Example",
                  "username" to "example1",
                  "userTravelList" to listOf("w2HGCwaJ4KgcXJ5nVxkF"),
                  "needsOnboarding" to true))
          .await()

      auth.signOut()
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      firestore.terminate().await()
      auth.signOut()
      auth
          .signInWithEmailAndPassword("travelpouchtest2@gmail.com", "travelpouchtest2password")
          .await()
      val uid = auth.currentUser!!.uid
      auth.currentUser!!.delete().await()
    }
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
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("example1@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("password1")
        composeTestRule.onNodeWithText("Log in").performClick()

        // Skip onboarding
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("OnboardingScreen", useUnmergedTree = true).isDisplayed()
        }
        composeTestRule.onNodeWithTag("SkipButton").performClick()

        // wait until we are in the travel list screen
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("emptyTravelPrompt", useUnmergedTree = true).isDisplayed()
        }

        // test that no travels are displayed because we have a new account
        composeTestRule
            .onNodeWithTag("emptyTravelPrompt", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("createTravelFab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("createTravelFab").performClick()

        // wait until we are in the screen to add a travel
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
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
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
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
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
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
