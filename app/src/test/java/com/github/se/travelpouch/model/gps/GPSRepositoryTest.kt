// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.gps

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class GPSRepositoryUnitTest {

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var repository: GPSRepository

  @Before
  fun setUp() {
    // Mock the FusedLocationProviderClient
    fusedLocationClient = mock(FusedLocationProviderClient::class.java)

    // Instantiate the GPSRepository
    repository = GPSRepository(fusedLocationClient)
  }

  @Test
  fun testGetGPSUpdatesEmitsLocationUpdates() = runBlocking {
    // Simulate a location
    val mockLocation =
        mock(Location::class.java).apply {
          `when`(latitude).thenReturn(48.8566)
          `when`(longitude).thenReturn(2.3522)
        }
    val locationResult = LocationResult.create(listOf(mockLocation))

    // Simulate the behavior of fusedLocationClient
    doAnswer { invocation ->
          val callback = invocation.arguments[1] as LocationCallback
          // Call onLocationResult with the simulated result
          callback.onLocationResult(locationResult)

          // Return a successful Task<Void>
          Tasks.forResult(null)
        }
        .`when`(fusedLocationClient)
        .requestLocationUpdates(
            any(LocationRequest::class.java), any(LocationCallback::class.java), any())

    // Collect the first emitted location
    val emittedLocation = repository.getGPSUpdates().first()

    // Verify the results
    assertEquals(48.8566, emittedLocation?.latitude)
    assertEquals(2.3522, emittedLocation?.longitude)
  }

  @Test
  fun testGetGPSUpdatesHandlesErrors() = runBlocking {
    // Simulate a location to verify the callback is called correctly
    val mockLocation =
        mock(Location::class.java).apply {
          `when`(latitude).thenReturn(48.8566)
          `when`(longitude).thenReturn(2.3522)
        }
    val locationResult = LocationResult.create(listOf(mockLocation))

    // Configure fusedLocationClient to return a failed Task
    doAnswer { invocation ->
          val callback = invocation.arguments[1] as LocationCallback
          // Call onLocationResult to simulate a location
          callback.onLocationResult(locationResult)

          // Throw a simulated exception after emitting a location
          throw Exception("Permission denied")
        }
        .`when`(fusedLocationClient)
        .requestLocationUpdates(
            any(LocationRequest::class.java), any(LocationCallback::class.java), any())

    // Collect data and handle exceptions
    var caughtException: Exception? = null

    try {
      repository.getGPSUpdates().first() // This should throw an exception
    } catch (e: Exception) {
      caughtException = e
    }

    // Verify that an exception was thrown
    assertTrue(caughtException is Exception)
    assertEquals("Permission denied", caughtException?.message)
  }

  @Test
  fun testGetGPSUpdatesForMapEmitsLatLng() = runBlocking {
    val mockLocation =
        mock(Location::class.java).apply {
          `when`(latitude).thenReturn(48.8566)
          `when`(longitude).thenReturn(2.3522)
        }
    val locationResult = LocationResult.create(listOf(mockLocation))

    doAnswer { invocation ->
          val callback = invocation.arguments[1] as LocationCallback
          callback.onLocationResult(locationResult)
          Tasks.forResult(null)
        }
        .`when`(fusedLocationClient)
        .requestLocationUpdates(
            any(LocationRequest::class.java), any(LocationCallback::class.java), any())

    val emittedLatLng = repository.getGPSUpdatesForMap().first()

    assertNotNull(emittedLatLng)
    assertNotNull(emittedLatLng?.latitude)
    assertNotNull(emittedLatLng?.longitude)

    assertEquals(48.8566, emittedLatLng!!.latitude, 0.0001)
    assertEquals(2.3522, emittedLatLng.longitude, 0.0001)
  }

  @Test
  fun testGetCurrentLocationSuccess() {
    val mockLocation =
        mock(Location::class.java).apply {
          `when`(latitude).thenReturn(48.8566)
          `when`(longitude).thenReturn(2.3522)
        }
    val locationResult = LocationResult.create(listOf(mockLocation))

    doAnswer { invocation ->
          val callback = invocation.arguments[1] as LocationCallback
          callback.onLocationResult(locationResult)
          Tasks.forResult(null)
        }
        .`when`(fusedLocationClient)
        .requestLocationUpdates(
            any(LocationRequest::class.java), any(LocationCallback::class.java), any())

    var receivedLocation: Location? = null
    var error: Exception? = null

    repository.getCurrentLocation(
        onSuccess = { location -> receivedLocation = location },
        onFailure = { exception -> error = exception })

    assertNotNull(receivedLocation)
    assertEquals(48.8566, receivedLocation?.latitude)
    assertEquals(2.3522, receivedLocation?.longitude)
    assertNull(error)
  }
}
