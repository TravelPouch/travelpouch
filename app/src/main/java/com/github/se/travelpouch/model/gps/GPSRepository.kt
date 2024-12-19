// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.gps

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/**
 * Repository for handling GPS location updates. This class provides a Flow for listening to
 * continuous GPS location updates and a possibility to request a single location update.
 *
 * @param fusedLocationClient The FusedLocationProviderClient instance to use for location updates.
 */
class GPSRepository(private val fusedLocationClient: FusedLocationProviderClient) {

  companion object {
    private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
    private const val LOCATION_FASTEST_INTERVAL = 5000L // 5 seconds
    private const val TAG = "GPSRepository"
  }

  /**
   * Provides a Flow for listening to continuous GPS location updates. Consumers of this Flow will
   * receive the latest location data.
   */
  @SuppressLint("MissingPermission") // Ensure permissions are handled elsewhere
  fun getGPSUpdates(): Flow<Location?> = callbackFlow {
    val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            .setWaitForAccurateLocation(true) // Ensures more precise location updates
            .build()

    val locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location -> trySend(location) }
          }
        }

    // Start location updates
    fusedLocationClient
        .requestLocationUpdates(locationRequest, locationCallback, null)
        .addOnFailureListener { exception ->
          Log.e(TAG, "Failed to request location updates", exception)
          close(exception)
        }

    // Stop location updates when Flow is closed
    awaitClose {
      fusedLocationClient.removeLocationUpdates(locationCallback)
      Log.d(TAG, "Location updates stopped")
    }
  }

  /** Provides a Flow for GPS updates directly formatted as LatLng for map usage. */
  @SuppressLint("MissingPermission") // Ensure permissions are handled elsewhere
  fun getGPSUpdatesForMap(): Flow<LatLng?> =
      getGPSUpdates().map { location -> location?.let { LatLng(it.latitude, it.longitude) } }

  /**
   * Requests a single location update and provides the location data to the onSuccess callback.
   *
   * @param onSuccess Callback that is called when the location is successfully fetched.
   * @param onFailure Callback that is called when the location request fails.
   */
  @SuppressLint("MissingPermission")
  fun getCurrentLocation(onSuccess: (Location) -> Unit, onFailure: (Exception) -> Unit) {
    val locationRequest =
        LocationRequest.Builder(
                LocationRequest.PRIORITY_HIGH_ACCURACY, 0 // Single request
                )
            .setWaitForAccurateLocation(true)
            .build()

    val locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {

              onSuccess(location) // Notify success
            } else {

              onFailure(Exception("Location is null")) // Handle edge case
            }
            fusedLocationClient.removeLocationUpdates(this)
          }
        }

    fusedLocationClient
        .requestLocationUpdates(locationRequest, locationCallback, null)
        .addOnFailureListener { ex ->
          Log.e("GPSRepository", "Failed to request single location update", ex)
          onFailure(ex) // Notify failure
          fusedLocationClient.removeLocationUpdates(locationCallback)
        }
  }
}
