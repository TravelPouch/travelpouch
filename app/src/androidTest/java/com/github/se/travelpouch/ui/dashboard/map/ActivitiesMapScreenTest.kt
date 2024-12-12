package com.github.se.travelpouch.ui.dashboard.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
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
import com.github.se.travelpouch.model.activity.map.RouteDetails
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockkStatic
import java.text.SimpleDateFormat
import java.util.Locale
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
              date = createTimestamp("20/12/2024 12:00"),
              documentsNeeded = mapOf("Agenda" to 1, "Meeting Notes" to 2)),
          Activity(
              uid = "2",
              title = "Client Presentation",
              description = "Presentation to showcase the project to the client.",
              location = Location(49.8566, 2.3522, Timestamp.now(), "Paris"),
              date = createTimestamp("21/12/2024 13:00"),
              documentsNeeded = null),
          Activity(
              uid = "3",
              title = "Workshop",
              description = "Workshop on team building and skill development.",
              location = Location(49.02, 2.5, Timestamp.now(), "Paris"),
              date = createTimestamp("23/12/2024 14:00"),
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

    // Mock the required permissions
    mockkStatic(ContextCompat::class)

    every {
      ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED
    every {
      ContextCompat.checkSelfPermission(any(), Manifest.permission.ACCESS_COARSE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED
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
    composeTestRule.setContent {
      ActivitiesMapScreen(
          activityViewModel = mockActivityModelView, navigationActions = mockNavigationActions)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("GoBackButton").performClick()

    verify(mockNavigationActions).goBack()
  }

  @Test
  fun testActivityDetailsContentDisplaysCorrectDetails() {
    composeTestRule.setContent { ActivityDetailsContent(activity = listOfActivities[0]) }

    // Verify title
    composeTestRule.onNodeWithTag("ActivityTitle").assertExists().assertTextEquals("Team Meeting")

    // Verify date (expected format: "20/12/2024")
    composeTestRule.onNodeWithTag("ActivityDate").assertExists().assertTextEquals("20/12/2024")

    // Verify time (expected format: "12:00")
    composeTestRule.onNodeWithTag("ActivityTime").assertExists().assertTextEquals("12:00")

    // Verify location
    composeTestRule.onNodeWithTag("ActivityLocation").assertExists().assertTextEquals("Paris")

    // Verify description
    composeTestRule
        .onNodeWithTag("ActivityDescription")
        .assertExists()
        .assertTextEquals("Monthly team meeting to discuss project progress.")
  }

  @Test
  fun testGpsDetailsContentDisplaysCorrectDetails() {
    composeTestRule.setContent {
      GpsDetailsContent(
          gpsRouteDetails =
              RouteDetails(
                  origin = LatLng(48.8566, 2.3522), // Mock origin (e.g., Paris)
                  destination = LatLng(49.8566, 2.3522), // Mock destination
                  route = listOf(LatLng(48.8566, 2.3522), LatLng(49.8566, 2.3522)), // Mock route
                  legsDistance = listOf("5 km"),
                  legsDuration = listOf("10 mins"),
                  legsRoute =
                      listOf(
                          listOf(
                              LatLng(48.8566, 2.3522),
                              LatLng(48.8570, 2.3530)) // Mock GPS to activity route
                          )),
          selectedActivity = listOfActivities[0] // Mock selected activity
          )
    }

    // Verify title
    composeTestRule
        .onNodeWithTag("RouteDetailsTitle_Gps")
        .assertExists()
        .assertTextEquals("Your current Location")

    // Verify distance
    composeTestRule
        .onNodeWithTag("RouteDetailsDistance_Gps")
        .assertExists()
        .assertTextEquals("Distance: 5 km")

    // Verify duration
    composeTestRule
        .onNodeWithTag("RouteDetailsDurationGps")
        .assertExists()
        .assertTextEquals("Duration: 10 mins")

    // Verify subtitle (Activity Title)
    composeTestRule
        .onNodeWithTag("RouteDetailsSubtitle_Gps")
        .assertExists()
        .assertTextEquals("Team Meeting")
  }

  @Test
  fun testLegDetailsContentDisplaysCorrectDetails() {
    composeTestRule.setContent {
      LegDetailsContent(
          routeDetails =
              RouteDetails(
                  origin = LatLng(48.8566, 2.3522), // Mock origin (Paris)
                  destination = LatLng(49.8566, 2.3522), // Mock destination
                  route = listOf(LatLng(48.8566, 2.3522), LatLng(49.8566, 2.3522)), // Mock route
                  legsDistance = listOf("3.4 km", "5.2 km"),
                  legsDuration = listOf("15 mins", "25 mins"),
                  legsRoute =
                      listOf(
                          listOf(LatLng(48.8566, 2.3522), LatLng(48.8570, 2.3530)), // Mock leg 1
                          listOf(LatLng(48.8570, 2.3530), LatLng(49.8566, 2.3522)) // Mock leg 2
                          )),
          legIndex = 0,
          activities = listOfActivities)
    }

    // Verify title (start activity)
    composeTestRule
        .onNodeWithTag("RouteDetailsTitle_Leg")
        .assertExists()
        .assertTextEquals("Team Meeting")

    // Verify distance
    composeTestRule
        .onNodeWithTag("RouteDetailsDistance_Leg")
        .assertExists()
        .assertTextEquals("Distance: 3.4 km")

    // Verify duration
    composeTestRule
        .onNodeWithTag("RouteDetailsDurationLeg")
        .assertExists()
        .assertTextEquals("Duration: 15 mins")

    // Verify subtitle (end activity)
    composeTestRule
        .onNodeWithTag("RouteDetailsSubtitle_Leg")
        .assertExists()
        .assertTextEquals("Client Presentation")
  }
}

fun createTimestamp(dateString: String, format: String = "dd/MM/yyyy HH:mm"): Timestamp {
  val dateFormat = SimpleDateFormat(format, Locale.getDefault())
  val date = dateFormat.parse(dateString) ?: throw IllegalArgumentException("Invalid date format")
  return Timestamp(date)
}
