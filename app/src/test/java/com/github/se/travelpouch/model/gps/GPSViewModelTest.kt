package com.github.se.travelpouch.model.gps

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GPSViewModelTest {

  private lateinit var gpsRepository: GPSRepository
  private lateinit var viewModel: GPSViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    // Mock the GPSRepository
    gpsRepository = mock(GPSRepository::class.java)

    // Set the dispatcher for coroutines
    Dispatchers.setMain(testDispatcher)

    // Instantiate the ViewModel with the mocked repository
    viewModel = GPSViewModel(gpsRepository)
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher to avoid affecting other tests
    Dispatchers.resetMain()
  }

  @Test
  fun testRealTimeLocationUpdatesAfterStartingUpdates() = runBlocking {
    // Simulate a LatLng
    val mockLocation = LatLng(48.8566, 2.3522)

    // Simulate repository emitting the location
    `when`(gpsRepository.getGPSUpdatesForMap()).thenReturn(flow { emit(mockLocation) })

    // Start real-time updates
    viewModel.startRealTimeLocationUpdates()

    // Collect the first emitted value from the ViewModel's StateFlow
    val emittedLocation = viewModel.realTimeLocation.first()

    // Verify the result
    assertNotNull(emittedLocation)
    assertEquals(48.8566, emittedLocation?.latitude)
    assertEquals(2.3522, emittedLocation?.longitude)
  }

  @Test
  fun testRealTimeLocationUpdatesHandlesNull() = runBlocking {
    // Simulate the repository emitting null values
    `when`(gpsRepository.getGPSUpdatesForMap()).thenReturn(flowOf(null))

    // Collect the first emitted value from the ViewModel's StateFlow
    val emittedLocation = viewModel.realTimeLocation.first()

    // Verify the result is null
    assertNull(emittedLocation)
  }

  @Test
  fun testFetchCurrentLocationSuccess() = runBlocking {
    // Simulate a successful location
    val mockLocation =
        mock(android.location.Location::class.java).apply {
          `when`(latitude).thenReturn(48.8566)
          `when`(longitude).thenReturn(2.3522)
        }
    // Simulate repository providing the location successfully
    doAnswer {
          val onSuccess = it.getArgument(0) as (Location) -> Unit
          onSuccess(mockLocation)
          null
        }
        .whenever(gpsRepository)
        .getCurrentLocation(onSuccess = any(), onFailure = any())

    // Call the ViewModel's method
    viewModel.fetchCurrentLocation()

    // Collect the first emitted value from the ViewModel's StateFlow
    val emittedLocation = viewModel.singleLocationUpdate.first()

    // Verify the result
    assertNotNull(emittedLocation)
    assertEquals(48.8566, emittedLocation?.latitude)
    assertEquals(2.3522, emittedLocation?.longitude)
  }

  @Test
  fun testFetchCurrentLocationFailure() = runBlocking {
    // Simulate an error in location retrieval
    doAnswer { invocation ->
          val onFailure = invocation.arguments[1] as (Exception) -> Unit
          onFailure(Exception("Location error"))
          null
        }
        .`when`(gpsRepository)
        .getCurrentLocation(any<(android.location.Location) -> Unit>(), any())

    // Call the ViewModel's method
    viewModel.fetchCurrentLocation()

    // Collect the first emitted value from the ViewModel's StateFlow
    val emittedLocation = viewModel.singleLocationUpdate.first()

    // Verify the result is null
    assertNull(emittedLocation)
  }
}
