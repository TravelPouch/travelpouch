// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.activity.map

import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.travels.Location
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

class DirectionsViewModelTest {

  private lateinit var mockRepository: DirectionsRepository
  private lateinit var viewModel: DirectionsViewModel

  // Create a test dispatcher for the async operations
  private val testDispatcher = StandardTestDispatcher()

  val listOfActivities =
      listOf(
          Activity(
              "uid",
              "title",
              "description",
              Location(0.0, 0.0, Timestamp(0, 0), "location"),
              Timestamp(0, 0),
              emptyList()),
          Activity(
              "uid2",
              "title2",
              "description2",
              Location(0.0, 0.0, Timestamp(0, 0), "location2"),
              Timestamp(50, 0),
              emptyList()))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    mockRepository = mock(DirectionsRepository::class.java)
    viewModel = DirectionsViewModel(mockRepository)

    // Set the Main dispatcher to the test dispatcher
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    // Reset the Main dispatcher to the original
    Dispatchers.resetMain()
  }

  @Test
  fun fetchDirectionsShouldUpdatePathPointsOnSuccess() = runTest {
    val mockLeg =
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
    val mockResponse =
        DirectionsResponse(
            routes = listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"), legs = listOf(mockLeg))))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(4) as (DirectionsResponse) -> Unit
          onSuccess(mockResponse)
          null
        }
        .whenever(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyOrNull(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForActivities(listOfActivities, "driving")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.activitiesRouteDetails.value
    assertTrue(routeDetails != null)
    assertTrue(routeDetails!!.route.isNotEmpty())
  }

  @Test
  fun fetchDirectionsShouldHandleFailure() = runTest {
    // Arrange
    val mockLatLng = LatLng(37.7749, -122.4194)
    val exception = Exception("Network error")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(5)
          onFailure(exception)
          null
        }
        .`when`(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyString(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForActivities(listOfActivities, "driving")
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.activitiesRouteDetails.value
    assertTrue(routeDetails == RouteDetails.EMPTY) // Expect pathPoints to remain empty on failure
  }

  @Test
  fun extractPathPointsShouldHandleInvalidPolyline() = runTest {
    // Arrange
    val mockLatLng = LatLng(37.7749, -122.4194)

    val mockLeg =
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
    val mockResponse =
        DirectionsResponse(
            routes = listOf(Route(OverviewPolyline("INVALID_POLYLINE"), legs = listOf(mockLeg))))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(DirectionsResponse) -> Unit>(4)
          onSuccess(mockResponse)
          null
        }
        .`when`(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyString(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForActivities(listOfActivities, "driving")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.activitiesRouteDetails.value
    assertTrue(routeDetails == null || routeDetails.route.isEmpty())
  }

  @Test
  fun extractRouteDetailsShouldHandleMalformedLegs() = runTest {

    // Create a malformed leg with an empty polyline
    val malformedLeg =
        Leg(
            distanceText = "0 km",
            distanceValue = 0,
            durationText = "0 mins",
            durationValue = 0,
            startAddress = "Malformed Start",
            endAddress = "Malformed End",
            startLocation = LatLng(0.0, 0.0),
            endLocation = LatLng(0.0, 0.0),
            overviewPolyline = OverviewPolyline("") // Invalid empty polyline
            )

    val mockResponse =
        DirectionsResponse(
            routes =
                listOf(Route(OverviewPolyline("INVALID_POLYLINE"), legs = listOf(malformedLeg))))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(DirectionsResponse) -> Unit>(4)
          onSuccess(mockResponse)
          null
        }
        .`when`(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyString(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForActivities(listOfActivities, "driving")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.activitiesRouteDetails.value
    assertTrue(routeDetails == RouteDetails.EMPTY)
  }

  @Test
  fun fetchDirectionsForGPSShouldCallFetchDirections() = runTest {
    val mockLatLng = LatLng(37.7749, -122.4194)

    val mockLeg =
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
    val mockResponse =
        DirectionsResponse(
            routes = listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"), legs = listOf(mockLeg))))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(4) as (DirectionsResponse) -> Unit
          onSuccess(mockResponse)
          null
        }
        .whenever(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyOrNull(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForGps(mockLatLng, listOfActivities[0], "driving")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.gpsRouteDetails.value
    assertTrue(routeDetails != null)
    assertTrue(routeDetails!!.route.isNotEmpty())
  }

  @Test
  fun fetchDirectionsForGPSNullGPSLocation() = runTest {
    val mockLeg =
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
    val mockResponse =
        DirectionsResponse(
            routes = listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"), legs = listOf(mockLeg))))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(4) as (DirectionsResponse) -> Unit
          onSuccess(mockResponse)
          null
        }
        .whenever(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyOrNull(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForGps(null, listOfActivities[0], "driving")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.gpsRouteDetails.value
    assertTrue(routeDetails == RouteDetails.EMPTY)
  }

  @Test
  fun fetchDirectionsForGPSNullActivity() = runTest {
    val mockLatLng = LatLng(37.7749, -122.4194)

    val mockLeg =
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
    val mockResponse =
        DirectionsResponse(
            routes = listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"), legs = listOf(mockLeg))))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument(4) as (DirectionsResponse) -> Unit
          onSuccess(mockResponse)
          null
        }
        .whenever(mockRepository)
        .getDirections(
            origin = anyString(),
            destination = anyString(),
            mode = anyString(),
            waypoints = anyOrNull(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirectionsForGps(mockLatLng, null, "driving")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val routeDetails = viewModel.gpsRouteDetails.value
    assertTrue(routeDetails == RouteDetails.EMPTY)
  }
}
