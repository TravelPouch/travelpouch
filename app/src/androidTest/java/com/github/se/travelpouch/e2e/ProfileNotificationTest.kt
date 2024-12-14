package com.github.se.travelpouch.e2e

import android.icu.util.GregorianCalendar
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.rule.IntentsTestRule
import com.github.se.travelpouch.MainActivity
import com.github.se.travelpouch.di.AppModule
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationSector
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.travels.Role
import com.google.firebase.Timestamp
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
class ProfileNotificationTest {

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
      val uid1 =
          auth
              .createUserWithEmailAndPassword("example5@example.com", "password5")
              .await()
              .user!!
              .uid

      auth.signOut()

      val uid2 =
          auth
              .createUserWithEmailAndPassword("example6@example.com", "password6")
              .await()
              .user!!
              .uid

      // The first user that wants to join a travel
      firestore
          .collection("userslist")
          .document(uid1)
          .set(
              mapOf(
                  "email" to "example5@example.com",
                  "friends" to emptyList<String>(),
                  "fsUid" to uid1,
                  "name" to "Example5",
                  "username" to "example5",
                  "userTravelList" to emptyList<String>(),
                  "needsOnboarding" to false))
          .await()

      // The new user that wants to send an invitation to the previous user
      firestore
          .collection("userslist")
          .document(uid2)
          .set(
              mapOf(
                  "email" to "example6@example.com",
                  "friends" to emptyList<String>(),
                  "fsUid" to uid2,
                  "name" to "Example6",
                  "username" to "example6",
                  "userTravelList" to listOf("notificationProfile1"),
                  "needsOnboarding" to false))
          .await()

      // The travel for both users
      firestore
          .collection("allTravels")
          .document("notificationProfile1")
          .set(
              mapOf(
                  "allAttachments" to emptyMap<String, String>(),
                  "allParticipants" to mapOf(uid2 to "OWNER"),
                  "description" to "Description of the test travel",
                  "endTime" to Timestamp(GregorianCalendar(2025, 8, 24).time),
                  "fsUid" to "notificationProfile1",
                  "listParticipant" to listOf(uid2),
                  "location" to
                      mapOf(
                          "insertTime" to Timestamp.now(),
                          "latitude" to 44.9305652,
                          "longitude" to 5.7630211,
                          "name" to
                              "trou, câble, Susville, Grenoble, Isère, Auvergne-Rhône-Alpes, France métropolitaine, 38350, France"),
                  "startTime" to Timestamp(GregorianCalendar(2025, 8, 23).time),
                  "title" to "Test Notification Profile"))
          .await()

      firestore
          .collection("notifications")
          .document("QWERTZuiopasdfghjkly")
          .set(
              Notification(
                  "QWERTZuiopasdfghjkly",
                  uid2,
                  uid1,
                  "notificationProfile1",
                  NotificationContent.InvitationNotification(
                      "Example6", "Test Notification Profile", Role.PARTICIPANT),
                  notificationType = NotificationType.INVITATION,
                  sector = NotificationSector.TRAVEL))
          .await()

      auth.signOut()
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      auth.signOut()
      auth.signInWithEmailAndPassword("example5@example.com", "password5").await()
      val uid1 = auth.currentUser!!.uid
      auth.currentUser!!.delete().await()

      auth.signOut()
      auth.signInWithEmailAndPassword("example6@example.com", "password6").await()
      val uid2 = auth.currentUser!!.uid
      auth.currentUser!!.delete().await()

      firestore.collection("allTravels").document("notificationProfile1").delete().await()
      firestore.collection("userslist").document(uid1).delete().await()
      firestore.collection("userslist").document(uid2).delete().await()
      firestore.collection("notifications").document("QWERTZuiopasdfghjkly").delete().await()

      auth.signOut()

      firestore.terminate().await()
    }
  }

  @Test
  fun userFlowToAddParticipantByNotification() =
      runTest(timeout = 40.seconds) {
        composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcomText").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in with email and password").assertIsDisplayed()

        // go to sign in screen with email and password and log in
        composeTestRule.onNodeWithText("Sign in with email and password").performClick()

        composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log in").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emailField").performTextInput("example5@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("password5")
        composeTestRule.onNodeWithText("Log in").performClick()

        // Go to menu
        composeTestRule.waitUntil(timeoutMillis = 10000) {
          composeTestRule.onNodeWithText("Create your first travel!").isDisplayed()
        }

        composeTestRule.onNodeWithText("Create your first travel!").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menuFab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menuFab").performClick()

        // verify that all tabs are displayed
        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log out").assertIsDisplayed()
        composeTestRule.onNodeWithText("Storage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()

        // Go to notifications and verify that notification is received + accept
        composeTestRule.onNodeWithText("Notifications").performClick()
        composeTestRule
            .onNodeWithText(
                "Example6 invited you to join the travel Test Notification Profile as a PARTICIPANT.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("ACCEPT").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()

        // Verify that the travel is displayed and that only two participants
        composeTestRule.waitUntil(timeoutMillis = 10000) {
          composeTestRule.onNodeWithText("Test Notification Profile").isDisplayed()
        }
        composeTestRule
            .onNodeWithText("Test Notification Profile")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("plusButton").performClick()
        composeTestRule.onNodeWithText("Manage participants", useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText("example5@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("example6@example.com").assertIsDisplayed()

        // Return to the travelList screen to send friend notification
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()

        // Go to profile screen
        composeTestRule.onNodeWithTag("menuFab").performClick()
        composeTestRule.onNodeWithText("Profile").performClick()

        // add a friend, which is example6@example.com
        composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Add Friend").assertIsDisplayed().performClick()
        composeTestRule
            .onNodeWithTag("addingFriendField", useUnmergedTree = true)
            .assertIsDisplayed()
            .performTextClearance()
        composeTestRule
            .onNodeWithTag("addingFriendField", useUnmergedTree = true)
            .performTextInput("example6@example.com")
        composeTestRule.onNodeWithTag("addingFriendButton").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("menuFab").performClick()
        composeTestRule.onNodeWithText("Log out").performClick()

        // After log out, we log in with example6@example.com a accept friend invitation
        composeTestRule.onNodeWithText("Sign in with email and password").performClick()
        composeTestRule.onNodeWithTag("emailField").performTextInput("example6@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("password6")
        composeTestRule.onNodeWithText("Log in").performClick()

        // Go to the travel and verify that example5@example.com was indeed added to both users
        composeTestRule.waitUntil(timeoutMillis = 10000) {
          composeTestRule.onNodeWithText("Test Notification Profile").isDisplayed()
        }

        composeTestRule
            .onNodeWithText("Test Notification Profile")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("plusButton").performClick()
        composeTestRule.onNodeWithText("Manage participants", useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText("example5@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("example6@example.com").assertIsDisplayed()

        // Return to the travelList screen to accept friend notification
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("menuFab").performClick()
        composeTestRule.onNodeWithText("Notifications").performClick()
        composeTestRule
            .onNodeWithText("example5@example.com wants to be your friend")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("ACCEPT").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("menuFab").performClick()
        composeTestRule.onNodeWithText("Profile").performClick()

        composeTestRule.onNodeWithText("example5@example.com").assertIsDisplayed()

        // Log out
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("menuFab").performClick()
        composeTestRule.onNodeWithText("Log out").performClick()

        // Connect with example5@example.com to verify that example6@exmaple.com is indeed our
        // friend
        composeTestRule.onNodeWithText("Sign in with email and password").performClick()
        composeTestRule.onNodeWithTag("emailField").performTextInput("example5@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("password5")
        composeTestRule.onNodeWithText("Log in").performClick()

        // We verify that we still have the travel displayed
        composeTestRule.waitUntil(timeoutMillis = 10000) {
          composeTestRule.onNodeWithText("Test Notification Profile").isDisplayed()
        }

        composeTestRule.onNodeWithTag("menuFab").performClick()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("example6@example.com").assertIsDisplayed()
      }
}
