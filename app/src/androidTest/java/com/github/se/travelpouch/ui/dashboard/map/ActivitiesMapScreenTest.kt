package com.github.se.travelpouch.ui.dashboard.map

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ActivitiesMapScreenTest {

  private lateinit var mockActivityRepositoryFirebase: ActivityRepository
  private lateinit var mockActivityModelView: ActivityViewModel
  private lateinit var mockNavigationActions: NavigationActions

  val listOfActivities =
      listOf(
          Activity(
              uid = "1",
              title = "Team Meeting",
              description = "Monthly team meeting to discuss project progress.",
              location = Location(48.8566, 2.3522, Timestamp.now(), "Paris"),
              date = Timestamp.now(),
              documentsNeeded = mapOf("Agenda" to 1, "Meeting Notes" to 2)),
          Activity(
              uid = "2",
              title = "Client Presentation",
              description = "Presentation to showcase the project to the client.",
              location = Location(40.0, -122.4194, Timestamp.now(), "Paris"),
              date = Timestamp(Timestamp.now().seconds + 3600, Timestamp.now().nanoseconds),
              documentsNeeded = null))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityModelView = ActivityViewModel(mockActivityRepositoryFirebase)
  }

  @Test
  fun displaysMarkersForActivities() {

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(0) as (List<Activity>) -> Unit
          onSuccess(listOfActivities)
          null
        }
        .whenever(mockActivityRepositoryFirebase)
        .getAllActivities(anyOrNull(), anyOrNull())

    mockActivityModelView.getAllActivities()

    composeTestRule.setContent { ActivitiesMapScreen(mockActivityModelView, mockNavigationActions) }

    composeTestRule.onNodeWithTag("Map").assertExists()
  }

  @Test
  fun displaysDefaultLocationWhenNoActivities() {

    `when`(mockActivityRepositoryFirebase.getAllActivities(any(), any())).then {
      it.getArgument<(List<Activity>) -> Unit>(0)(listOf())
    }
    composeTestRule.setContent { ActivitiesMapScreen(mockActivityModelView, mockNavigationActions) }

    composeTestRule.onNodeWithTag("Map").assertExists()
  }

  @Test
  fun testGoBackButton() {
    // Configurer le contenu de la règle Compose
    composeTestRule.setContent {
      ActivitiesMapScreen(
          activityViewModel = mockActivityModelView, navigationActions = mockNavigationActions)
    }

    // Attendre que l'interface utilisateur soit prête
    composeTestRule.waitForIdle()

    // Simuler le clic sur le bouton "Go Back"
    composeTestRule.onNodeWithTag("GoBackButton").performClick()

    // Vérifier que la méthode goBack() a été appelée
    verify(mockNavigationActions).goBack()
  }
}
