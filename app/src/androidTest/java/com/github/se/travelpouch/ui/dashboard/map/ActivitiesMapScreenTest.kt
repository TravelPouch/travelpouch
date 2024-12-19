// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.test.assert
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
              documentsNeeded = emptyList()),
          Activity(
              uid = "2",
              title = "Client Presentation",
              description = "Presentation to showcase the project to the client.",
              location = Location(49.8566, 2.3522, Timestamp.now(), "Paris"),
              date = createTimestamp("21/12/2024 13:00"),
              documentsNeeded = emptyList()),
          Activity(
              uid = "3",
              title = "Workshop",
              description = "Workshop on team building and skill development.",
              location = Location(49.02, 2.5, Timestamp.now(), "Paris"),
              date = createTimestamp("23/12/2024 14:00"),
              documentsNeeded = emptyList()))

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
    composeTestRule.setContent {
      ActivityDetailsContent(
          activity = listOfActivities[0], // Pass the first activity
          isGPSAvailable = true, // Mock GPS availability
          onShowPathGPS = {} // Provide a no-op lambda for the callback
          )
    }

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
                              LatLng(48.8570, 2.3530) // Mock GPS to activity route
                              ))),
          selectedActivity = listOfActivities[0], // Mock selected activity
          travelMode = "walking", // Mock travel mode
          onTravelModeSelected = {} // No-op lambda for testing
          )
    }

    // Verify title
    composeTestRule
        .onNodeWithTag("RouteDetailsTitle_Gps")
        .assertExists()
        .assertTextEquals("Your Current Location")

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
          legIndex = 0, // Testing the first leg
          activities = listOfActivities,
          travelMode = "walking" // Mock travel mode
          )
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

  @Test
  fun testTravelModeButtonActivities() {
    var showPathsState = false
    var selectedTravelMode = "walking"

    composeTestRule.setContent {
      TravelModeButtonActivities(
          showPaths = showPathsState,
          onTogglePaths = { showPathsState = !showPathsState },
          travelMode = selectedTravelMode,
          onTravelModeChange = { mode -> selectedTravelMode = mode })
    }

    // Verify initial state of toggle button
    composeTestRule.onNodeWithTag("ToggleModeButtonActivities").assertExists()

    // Click the toggle button to enable paths
    composeTestRule.onNodeWithTag("ToggleModeButtonActivities").performClick()
    assert(showPathsState) // Verify state is updated

    // Verify "Walking Mode" button exists
    composeTestRule.onNodeWithTag("WalkingModeButtonActivities").assertExists()

    // Switch to "Driving Mode"
    composeTestRule.onNodeWithTag("WalkingModeButtonActivities").performClick()
    assert(selectedTravelMode == "walking") // Verify state is updated

    // Verify "Driving Mode" button exists
    composeTestRule.onNodeWithTag("DrivingModeButtonActivities").assertExists()

    // Switch to "Driving Mode"
    composeTestRule.onNodeWithTag("DrivingModeButtonActivities").performClick()
    assert(selectedTravelMode == "driving") // Verify state is updated

    // Verify "Bicycling Mode" button exists
    composeTestRule.onNodeWithTag("BicyclingModeButtonActivities").assertExists()

    // Switch to "Bicycling Mode"
    composeTestRule.onNodeWithTag("BicyclingModeButtonActivities").performClick()
    assert(selectedTravelMode == "bicycling") // Verify state is updated
  }

  @Test
  fun testTravelModesGPS() {
    var selectedMode = "walking"

    composeTestRule.setContent {
      TravelModesGPS(selectedMode = selectedMode, onModeSelected = { mode -> selectedMode = mode })
    }

    // Switch to "walking" mode
    composeTestRule.onNodeWithTag("WalkingModeButtonGPS").assertExists()
    composeTestRule.onNodeWithTag("WalkingModeButtonGPS").performClick()
    assert(selectedMode == "walking") // Verify initial mode

    // Switch to "Car" mode
    composeTestRule.onNodeWithTag("DrivingModeButtonGPS").assertExists()
    composeTestRule.onNodeWithTag("DrivingModeButtonGPS").performClick()
    assert(selectedMode == "driving") // Verify mode is updated

    // Switch to "Bicycle" mode
    composeTestRule.onNodeWithTag("BicyclingModeButtonGPS").assertExists()
    composeTestRule.onNodeWithTag("BicyclingModeButtonGPS").performClick()
    assert(selectedMode == "bicycling") // Verify mode is updated

    // Switch to "None" mode
    composeTestRule.onNodeWithTag("NoneModeButtonGPS").assertExists()
    composeTestRule.onNodeWithTag("NoneModeButtonGPS").performClick()
    assert(selectedMode == "None") // Verify mode is updated
  }
}

fun createTimestamp(dateString: String, format: String = "dd/MM/yyyy HH:mm"): Timestamp {
  val dateFormat = SimpleDateFormat(format, Locale.getDefault())
  val date = dateFormat.parse(dateString) ?: throw IllegalArgumentException("Invalid date format")
  return Timestamp(date)
}
