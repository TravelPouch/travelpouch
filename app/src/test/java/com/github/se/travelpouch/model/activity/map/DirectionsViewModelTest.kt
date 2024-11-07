package com.github.se.travelpouch.model.activity.map

import com.google.android.gms.maps.model.LatLng
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

class DirectionsViewModelTest {

  private lateinit var mockRepository: DirectionsRepositoryInterface
  private lateinit var viewModel: DirectionsViewModel

  // Create a test dispatcher for the async operations
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    mockRepository = mock(DirectionsRepositoryInterface::class.java)
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
    // Arrange
    val mockLatLng = LatLng(37.7749, -122.4194)
    val mockResponse =
        DirectionsResponse(routes = listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"))))

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
            apiKey = anyString(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirections(mockLatLng, mockLatLng, "driving", "mockApiKey")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val pathPoints = viewModel.routeOptionsList.value
    assertTrue(pathPoints.isNotEmpty())
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
            apiKey = anyString(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirections(mockLatLng, mockLatLng, "driving", "mockApiKey")
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val pathPoints = viewModel.routeOptionsList.value
    assertTrue(pathPoints.isEmpty()) // Expect pathPoints to remain empty on failure
  }

  @Test
  fun extractPathPointsShouldHandleInvalidPolyline() = runTest {
    // Arrange
    val mockLatLng = LatLng(37.7749, -122.4194)
    val mockResponse =
        DirectionsResponse(routes = listOf(Route(OverviewPolyline("INVALID_POLYLINE"))))

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
            apiKey = anyString(),
            onSuccess = any(),
            onFailure = any())

    // Act
    viewModel.fetchDirections(mockLatLng, mockLatLng, "driving", "mockApiKey")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val pathPoints = viewModel.routeOptionsList.value
    assertTrue(pathPoints.isEmpty())
  }

  @Test
  fun clearRoutesShouldResetRouteOptionsList() = runTest {
    // Arrange
    val mockLatLng = LatLng(37.7749, -122.4194)
    val mockResponse =
        DirectionsResponse(routes = listOf(Route(OverviewPolyline("u{~vFvyys@fC_y@"))))

    // Populate the route options list by fetching directions
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
            apiKey = anyString(),
            onSuccess = any(),
            onFailure = any())

    viewModel.fetchDirections(mockLatLng, mockLatLng, "driving", "mockApiKey")

    // Advance the dispatcher to execute pending coroutines
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert that the route options list is not empty
    assertTrue(viewModel.routeOptionsList.value.isNotEmpty())

    // Act
    viewModel.clearRoutesOptions()

    // Assert that the route options list is now empty
    assertTrue(viewModel.routeOptionsList.value.isEmpty())
  }
}
