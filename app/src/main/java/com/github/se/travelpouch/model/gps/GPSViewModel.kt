// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.gps

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the GPS feature.
 *
 * @param gpsRepository Repository for the GPS feature.
 * @constructor Creates a GPSViewModel.
 */
class GPSViewModel(private val gpsRepository: GPSRepository) : ViewModel() {

  // Holds the real-time continuous location updates as a StateFlow
  private val _realTimeLocation = MutableStateFlow<LatLng?>(null)
  val realTimeLocation: StateFlow<LatLng?>
    get() = _realTimeLocation

  // Holds the single location update as a StateFlow
  private val _singleLocationUpdate = MutableStateFlow<LatLng?>(null)
  val singleLocationUpdate: StateFlow<LatLng?>
    get() = _singleLocationUpdate

  // Tracks whether the GPS has been started
  private val _hasStartedGPS = MutableStateFlow(false)
  val hasStartedGPS: StateFlow<Boolean>
    get() = _hasStartedGPS

  init {
    Log.d("GPSViewModel", "init")
  }

  /** Start the real-time location updates and update the StateFlow. */
  fun startRealTimeLocationUpdates() {

    if (_hasStartedGPS.value) {
      Log.d("GPSViewModel", "GPS updates already started")
      return
    }

    Log.d("GPSViewModel", "startRealTimeLocationUpdates")
    _hasStartedGPS.value = true

    viewModelScope.launch {
      gpsRepository
          .getGPSUpdatesForMap()
          .catch {
            Log.e("GPSViewModel", "Error: $it")

            _realTimeLocation.value = null // Reset the StateFlow
            _hasStartedGPS.value = false // Reset the tracking state
          }
          .collect { location ->
            Log.d("GPSViewModel", "Location: $location")
            _realTimeLocation.value = location
          }
    }
  }

  /**
   * Stops the real-time location updates and resets the tracking state.
   *
   * Note: This method is not currently in use but might be useful in the future for stopping
   * real-time location tracking under certain conditions without destroying the ViewModel.
   */
  fun stopRealTimeLocationUpdates() {
    Log.d("GPSViewModel", "Stopping GPS updates")
    _hasStartedGPS.value = false // Reset the tracking state
    _realTimeLocation.value = null // Optionally reset the last known location
  }

  /** Fetch the current location (real-time only) and update the StateFlow. */
  fun fetchCurrentLocation() {

    viewModelScope.launch {
      gpsRepository.getCurrentLocation(
          onSuccess = { location ->
            _singleLocationUpdate.value = LatLng(location.latitude, location.longitude)
          },
          onFailure = { _singleLocationUpdate.value = null })
    }
  }

  /** Factory for creating GPSViewModel instances. */
  companion object {
    fun Factory(context: Context): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GPSViewModel::class.java)) {
              val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
              val gpsRepository = GPSRepository(fusedLocationClient)
              return GPSViewModel(gpsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }
}
