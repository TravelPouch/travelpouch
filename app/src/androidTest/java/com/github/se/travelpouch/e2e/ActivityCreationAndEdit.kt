// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.e2e

import android.icu.util.GregorianCalendar
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.di.AppModule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
class ActivityCreationAndEdit {

  private val DEFAULT_TIMEOUT = 10000L

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject lateinit var firestore: FirebaseFirestore
  @Inject lateinit var auth: FirebaseAuth

  @Before
  fun setUp() {
    hiltRule.inject()

    // seed DB with existing trave @Inject lateinit var auth: FirebaseAuthl and user
    runBlocking {
      val uid =
          auth.createUserWithEmailAndPassword("example3@example.com", "password").await().user!!.uid

      firestore
          .collection("allTravels")
          .document("w2HGCwaJ4KgcXJ5nVxkF")
          .set(
              mapOf(
                  "allAttachments" to emptyMap<String, String>(),
                  "allParticipants" to mapOf(uid to "OWNER"),
                  "description" to "Description of the test travel",
                  "endTime" to Timestamp(GregorianCalendar(2025, 8, 24).time),
                  "fsUid" to "w2HGCwaJ4KgcXJ5nVxkF",
                  "listParticipant" to listOf(uid),
                  "location" to
                      mapOf(
                          "insertTime" to Timestamp.now(),
                          "latitude" to 44.9305652,
                          "longitude" to 5.7630211,
                          "name" to
                              "trou, câble, Susville, Grenoble, Isère, Auvergne-Rhône-Alpes, France métropolitaine, 38350, France"),
                  "startTime" to Timestamp(GregorianCalendar(2025, 8, 23).time),
                  "title" to "Test"))
          .await()

      firestore
          .collection("userslist")
          .document(uid)
          .set(
              mapOf(
                  "email" to "example3@example.com",
                  "friends" to emptyList<String>(),
                  "fsUid" to uid,
                  "name" to "Example",
                  "username" to "example",
                  "userTravelList" to listOf("w2HGCwaJ4KgcXJ5nVxkF"),
                  "needsOnboarding" to true))
          .await()

      auth.signOut()
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      auth.signOut()
      auth.signInWithEmailAndPassword("example3@example.com", "password").await()
      val uid = auth.currentUser!!.uid
      auth.currentUser!!.delete().await()

      firestore
          .collection("allTravels/w2HGCwaJ4KgcXJ5nVxkF/activities")
          .get()
          .await()
          .documents
          .forEach { it.reference.delete().await() } // delete all activities
      firestore.collection("allTravels").document("w2HGCwaJ4KgcXJ5nVxkF").delete().await()
      firestore.collection("userslist").document(uid).delete().await()
      auth.signOut()
      firestore.terminate().await()
    }
  }

  @Test
  fun verifyActivityCreationAndEditFlow() =
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
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("example3@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("password")
        composeTestRule.onNodeWithText("Log in").performClick()

        // Skip onboarding
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("OnboardingScreen", useUnmergedTree = true).isDisplayed()
        }
        composeTestRule.onNodeWithTag("SkipButton").performClick()

        // wait until we are in the travel list screen
        composeTestRule.waitUntil(timeoutMillis = 2000) {
          composeTestRule.onNodeWithText("Test", useUnmergedTree = true).isDisplayed()
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelListItem").performClick()

        // assert that there are no activities at the moment
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT + 1) {
          composeTestRule.onNodeWithTag("emptyTravelBox", useUnmergedTree = true).isDisplayed()
        }

        // add an activity button
        composeTestRule.onNodeWithTag("addActivityButton").assertIsDisplayed().performClick()
        // add activity screen
        composeTestRule.onNodeWithTag("AddActivityScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelTitle").assertIsDisplayed()

        // fill in the activity fields
        composeTestRule.onNodeWithTag("titleField").performTextClearance()
        composeTestRule.onNodeWithTag("titleField").performTextInput("epic activity")

        composeTestRule.onNodeWithTag("descriptionField").performTextClearance()
        composeTestRule
            .onNodeWithTag("descriptionField")
            .performTextInput("this is an epic activity")

        composeTestRule.onNodeWithTag("dateField").performTextClearance()
        composeTestRule.onNodeWithTag("dateField").performTextInput("01022024")

        composeTestRule.onNodeWithTag("timeField").performTextClearance()
        composeTestRule.onNodeWithTag("timeField").performTextInput("15:24")

        composeTestRule.onNodeWithTag("inputTravelLocation").performTextClearance()
        composeTestRule.onNodeWithTag("inputTravelLocation").performTextInput("L")

        // wait to have La paz displayed
        composeTestRule.waitUntil(timeoutMillis = 4000) {
          composeTestRule.onNodeWithText("La Paz, Bolivia").isDisplayed()
        }

        composeTestRule.onNodeWithText("La Paz, Bolivia").performClick()
        // save it
        composeTestRule.onNodeWithText("Save").assertIsDisplayed().performClick()
        // there is an activity
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT + 2) {
          composeTestRule.onNodeWithTag("emptyTravel", useUnmergedTree = true).isNotDisplayed()
        }
        // check the activity is displayed
        composeTestRule.onNodeWithText("epic activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("epic activity").assert(hasText("epic activity"))
        composeTestRule.onNodeWithText("epic activity").assert(hasText("1/2/2024"))
        composeTestRule.onNodeWithText("epic activity").assert(hasText("La Paz, Bolivia"))

        // edit the activity
        composeTestRule.onNodeWithText("epic activity").performClick()
        composeTestRule.onNodeWithTag("EditActivityScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("titleField").assert(hasText("epic activity"))
        composeTestRule
            .onNodeWithTag("descriptionField")
            .assert(hasText("this is an epic activity"))
        composeTestRule.onNodeWithTag("dateField").assert(hasText("01/02/2024"))
        composeTestRule.onNodeWithTag("inputTravelLocation").assert(hasText("La Paz, Bolivia"))

        composeTestRule.onNodeWithTag("titleField").performTextClearance()
        composeTestRule.onNodeWithTag("titleField").performTextInput("more epic activity")

        composeTestRule.onNodeWithTag("descriptionField").performTextClearance()
        composeTestRule
            .onNodeWithTag("descriptionField")
            .performTextInput("this is a more epic activity")

        composeTestRule.onNodeWithTag("dateField").performTextClearance()
        composeTestRule.onNodeWithTag("dateField").performTextInput("02022024")
        // save the new info
        composeTestRule.onNodeWithText("Save").assertIsDisplayed().performClick()
        // check the activity is displayed
        composeTestRule.onNodeWithText("more epic activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("more epic activity").assert(hasText("more epic activity"))
        composeTestRule.onNodeWithText("more epic activity").assert(hasText("2/2/2024"))
        composeTestRule.onNodeWithText("more epic activity").assert(hasText("La Paz, Bolivia"))
      }
}
