package com.github.se.travelpouch.model.activity.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Define an alias type for a path, which is a list of LatLng points
typealias Path = List<LatLng>

// Data class to hold the different route between origin and destination
data class RouteOptions(
    val origin: LatLng,
    val destination: LatLng,
    var routes: List<Path> // List of routes, where each route is a list of LatLng points
)

/** ViewModel for fetching and managing directions data from the Google Maps Directions API. */
class DirectionsViewModel(private val repository: DirectionsRepositoryInterface) : ViewModel() {

  // StateFlow to hold all fetched routes for different origin-destination pairs
  private val _routeOptionsList = MutableStateFlow<List<RouteOptions>>(emptyList())
  val pathPoints: MutableStateFlow<List<RouteOptions>>
    val routeOptions: StateFlow<List<RouteOptions>> get() = _routeOptionsList

  /** Factory class for creating DirectionsViewModel instances. */
  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DirectionsViewModel(DirectionsRepository(OkHttpClient())) as T
          }
        }
  }

  /**
   * Fetches directions from the repository and decodes the polyline points.
   *
   * @param origin The starting point of the route as a LatLng object.
   * @param destination The ending point of the route as a LatLng object.
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   * @param apiKey The API key for the Google Maps Directions API.
   */
  fun fetchDirections(origin: LatLng, destination: LatLng, mode: String, apiKey: String) {

    val originStr = "${origin.latitude},${origin.longitude}"
    val destinationStr = "${destination.latitude},${destination.longitude}"

    viewModelScope.launch {
      repository.getDirections(
          origin = originStr,
          destination = destinationStr,
          mode = mode,
          apiKey = apiKey,
          onSuccess = { directionsResponse ->

            // Log the number of routes fetched
            val numberOfRoutes = directionsResponse.routes.size
            Log.d("DirectionsViewModel", "Number of routes fetched: $numberOfRoutes")

            val routes = extractPathPoints(directionsResponse)

            if (routes.isNotEmpty()) {
              // Accumulate new routes with old ones
              _routeOptionsList.value += RouteOptions(origin, destination, routes)
            }
          },
          onFailure = { exception ->
            Log.e("DirectionsViewModel", "Failed to fetch directions", exception)
          })
    }
  }

  /**
   * Extracts the lists of LatLng points from the DirectionsResponse.
   *
   * @param directionsResponse The response from the Directions API.
   * @return A list of lists, where each inner list represents the LatLng points for one route.
   */
  private fun extractPathPoints(directionsResponse: DirectionsResponse): List<List<LatLng>> {
    val allRoutesPoints = mutableListOf<Path>()

    // Loop through each route in the DirectionsResponse
    directionsResponse.routes.forEach { route ->
      try {
        // Decode the polyline points for each route
        val decodedPoints = PolyUtil.decode(route.overviewPolyline.points)
        // Add the decoded points for this route to the list
        allRoutesPoints.add(decodedPoints)
      } catch (e: Exception) {
        // Log the error and skip this route
        Log.e("DirectionsViewModel", "Error decoding polyline for a route", e)
      }
    }

    return allRoutesPoints
  }

  /** Clears the list of routes by setting it to an empty list. */
  fun clearRoutesOptions() {
    _routeOptionsList.value = emptyList() // Set the value to an empty list
    Log.d("DirectionsViewModel", "Route paths have been cleared")
  }
}
