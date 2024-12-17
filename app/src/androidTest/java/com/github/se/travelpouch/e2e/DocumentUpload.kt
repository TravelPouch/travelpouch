package com.github.se.travelpouch.e2e

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.icu.util.GregorianCalendar
import android.net.Uri
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.di.AppModule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.io.File
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

private const val DEFAULT_TIMEOUT = 10000L

@HiltAndroidTest
@UninstallModules(AppModule::class)
class DocumentUpload {
  private lateinit var file: File

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

    // seed the db
    runBlocking {
      val uid =
          auth
              .createUserWithEmailAndPassword("example2@example.com", "password2")
              .await()
              .user!!
              .uid

      firestore
          .collection("allTravels")
          .document("skibidiJ4KgcXJ5nVxkF")
          .set(
              mapOf(
                  "allAttachments" to emptyMap<String, String>(),
                  "allParticipants" to mapOf(uid to "OWNER"),
                  "description" to "Description of the test travel",
                  "endTime" to Timestamp(GregorianCalendar(2025, 8, 24).time),
                  "fsUid" to "skibidiJ4KgcXJ5nVxkF",
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
                  "email" to "example2@example.com",
                  "friends" to emptyList<String>(),
                  "fsUid" to uid,
                  "name" to "Example",
                  "username" to "example",
                  "userTravelList" to listOf("skibidiJ4KgcXJ5nVxkF"),
                  "needsOnboarding" to true))
          .await()

      auth.signOut()
    }

    val context = getInstrumentation().context
    file = File.createTempFile("mountain", ".png")
    context.resources.openRawResource(com.github.se.travelpouch.test.R.drawable.mountain).use {
      file.outputStream().use { output -> it.copyTo(output) }
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      auth.signOut()
      auth.signInWithEmailAndPassword("example2@example.com", "password2").await()
      val uid = auth.currentUser!!.uid
      auth.currentUser!!.delete().await()

      firestore
          .collection("allTravels/skibidiJ4KgcXJ5nVxkF/documents")
          .get()
          .await()
          .documents
          .forEach { it.reference.delete().await() }
      firestore.collection("allTravels").document("skibidiJ4KgcXJ5nVxkF").delete().await()
      firestore.collection("userslist").document(uid).delete().await()
      auth.signOut()
      firestore.terminate().await()
      file.delete()
    }
  }

  @Test
  fun userFlowForDocumentUpload() =
      runTest(timeout = 30.seconds) {
        // mock the file picker
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            .respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK, Intent().setData(Uri.fromFile(file))))
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))
            .respondWith(
                Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(Uri.EMPTY)))

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

        composeTestRule.onNodeWithTag("emailField").performTextInput("example2@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("password2")
        composeTestRule.onNodeWithText("Log in").performClick()

        // Skip onboarding
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("OnboardingScreen", useUnmergedTree = true).isDisplayed()
        }
        composeTestRule.onNodeWithTag("SkipButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule
              .onNodeWithText("Description of the test travel", useUnmergedTree = true)
              .isDisplayed()
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
        composeTestRule.onNodeWithTag("travelListItem").performClick()

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("emptyTravel1", useUnmergedTree = true).isDisplayed()
          composeTestRule.onNodeWithTag("emptyTravel2", useUnmergedTree = true).isDisplayed()
        }

        composeTestRule.onNodeWithTag("travelActivitiesScreen").performTouchInput { swipeLeft() }

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule
              .onNodeWithTag("calendarScreenColumn", useUnmergedTree = true)
              .isDisplayed()
        }

        composeTestRule.onNodeWithTag("calendarScreenColumn").performTouchInput { swipeLeft() }

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("documentListScreen", useUnmergedTree = true).isDisplayed()
        }

        composeTestRule.onNodeWithTag("plusButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("plusButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 200) {
          composeTestRule.onNodeWithTag("dropDownButton", useUnmergedTree = true).isDisplayed()
        }

        composeTestRule.onNodeWithTag("importLocalFileButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scanCamButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("importLocalFileButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule.onNodeWithTag("documentListItem", useUnmergedTree = true).isDisplayed()
        }

        val newDocumentRefId =
            firestore
                .collection("allTravels/skibidiJ4KgcXJ5nVxkF/documents")
                .get()
                .await()
                .documents
                .first()
                .reference
                .id

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule
              .onNodeWithTag("thumbnail-$newDocumentRefId", useUnmergedTree = true)
              .isDisplayed()
        }
        composeTestRule.onNodeWithTag("documentListItem").assertIsDisplayed()
        composeTestRule.onNodeWithTag("documentListItem").performClick()

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_TIMEOUT) {
          composeTestRule.onNodeWithTag("documentPreviewScreen").isDisplayed()
        }
      }
}
