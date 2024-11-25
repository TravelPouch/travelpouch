package com.github.se.travelpouch.ui.dashboard.map

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityRepository
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.activity.map.DirectionsRepositoryInterface
import com.github.se.travelpouch.model.activity.map.DirectionsResponse
import com.github.se.travelpouch.model.activity.map.DirectionsViewModel
import com.github.se.travelpouch.model.activity.map.Leg
import com.github.se.travelpouch.model.activity.map.OverviewPolyline
import com.github.se.travelpouch.model.activity.map.Route
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyString
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
  private lateinit var mockkDirectionsViewModel: DirectionsViewModel
  private lateinit var mockkDirectionsRepository: DirectionsRepositoryInterface

  private val listOfActivities =
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
              documentsNeeded = null),
          Activity(
              uid = "3",
              title = "Workshop",
              description = "Workshop on team building and skill development.",
              location = Location(51.5074, -0.1278, Timestamp.now(), "London"),
              date = Timestamp(Timestamp.now().seconds + 7200, Timestamp.now().nanoseconds),
              documentsNeeded = mapOf("Workshop Material" to 1)))

  private val mockLeg =
      Leg(
          distanceText = "3.4 km",
          distanceValue = 3400,
          durationText = "15 mins",
          durationValue = 900,
          startAddress = "Start Address",
          endAddress = "End Address",
          startLocation = LatLng(37.7749, -122.4194),
          endLocation = LatLng(34.0522, -118.2437),
          overviewPolyline = OverviewPolyline("u{~vFvyys@fC_y@"))

  private val mockLeg2 =
      Leg(
          distanceText = "5.2 km",
          distanceValue = 5200,
          durationText = "25 mins",
          durationValue = 1500,
          startAddress = "End Address",
          endAddress = "Next Address",
          startLocation = LatLng(34.0522, -118.2437),
          endLocation = LatLng(36.1699, -115.1398),
          overviewPolyline = OverviewPolyline("a~bcFghij@dE_g@"))

  private val mockResponse =
      DirectionsResponse(
          routes =
              listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"), legs = listOf(mockLeg, mockLeg2))))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    mockActivityRepositoryFirebase = mock(ActivityRepository::class.java)
    mockActivityModelView = ActivityViewModel(mockActivityRepositoryFirebase)

    mockkDirectionsRepository = mock(DirectionsRepositoryInterface::class.java)
    mockkDirectionsViewModel = DirectionsViewModel(mockkDirectionsRepository)

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(4) as (DirectionsResponse) -> Unit
          onSuccess(mockResponse)
          null
        }
        .whenever(mockkDirectionsRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyOrNull(),
            onSuccess = any(),
            onFailure = any())
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

    composeTestRule.setContent {
      ActivitiesMapScreen(mockActivityModelView, mockNavigationActions, mockkDirectionsViewModel)
    }

    composeTestRule.onNodeWithTag("Map").assertExists()
  }

  @Test
  fun displaysDefaultLocationWhenNoActivities() {

    `when`(mockActivityRepositoryFirebase.getAllActivities(any(), any())).then {
      it.getArgument<(List<Activity>) -> Unit>(0)(listOf())
    }
    composeTestRule.setContent {
      ActivitiesMapScreen(mockActivityModelView, mockNavigationActions, mockkDirectionsViewModel)
    }

    composeTestRule.onNodeWithTag("Map").assertExists()
  }

  @Test
  fun testGoBackButton() {
    composeTestRule.setContent {
      ActivitiesMapScreen(
          activityViewModel = mockActivityModelView,
          navigationActions = mockNavigationActions,
          directionsViewModel = mockkDirectionsViewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("GoBackButton").performClick()

    verify(mockNavigationActions).goBack()
  }
}
