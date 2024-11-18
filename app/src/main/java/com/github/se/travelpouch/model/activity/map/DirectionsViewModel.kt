package com.github.se.travelpouch.model.activity.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.travelpouch.model.activity.Activity
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Define an alias type for a path, which is a list of LatLng points
typealias Path = List<LatLng>

// Data class to hold the details of the route between origin and destination
data class RouteDetails(
    val origin: LatLng,
    val destination: LatLng,
    val route: Path,
    val legsRoute: List<Path>
)

/** ViewModel for fetching and managing directions data from the Google Maps Directions API. */
class DirectionsViewModel(private val repository: DirectionsRepositoryInterface) : ViewModel() {

  // StateFlow to hold the fetched route details for activities
  private val _activityRouteDetails = MutableStateFlow<RouteDetails?>(null)
  val activityRouteDetails: StateFlow<RouteDetails?>
    get() = _activityRouteDetails

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

  /** Function to fetch directions between activities sequentially. */
  fun fetchDirectionsForActivities(activities: List<Activity>, mode: String, apiKey: String) {
    if (activities.size < 2) {
      Log.e("DirectionsViewModel", "Not enough activities to create a route")
      return
    }

    // Extract the origin from the first activity and the destination from the last activity
    val origin = LatLng(activities.first().location.latitude, activities.first().location.longitude)
    val destination =
        LatLng(activities.last().location.latitude, activities.last().location.longitude)

    // Extract the waypoints from the activities in between
    val waypoints =
        activities.subList(1, activities.size - 1).map {
          LatLng(it.location.latitude, it.location.longitude)
        }

    // Fetch directions using the extracted origin, destination, and waypoints
    fetchDirections(origin, destination, mode, apiKey, waypoints)
  }

  /**
   * Fetches directions from the repository and decodes the polyline points.
   *
   * @param origin The starting point of the route as a LatLng object.
   * @param destination The ending point of the route as a LatLng object.
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   * @param apiKey The API key for the Google Maps Directions API.
   */
  fun fetchDirections(
      origin: LatLng,
      destination: LatLng,
      mode: String,
      apiKey: String,
      waypoints: List<LatLng>? = null
  ) {

    val originStr = "${origin.latitude},${origin.longitude}"
    val destinationStr = "${destination.latitude},${destination.longitude}"

    val waypointsStr = waypoints?.joinToString("|") { "${it.latitude},${it.longitude}" }

    viewModelScope.launch {
      repository.getDirections(
          origin = originStr,
          destination = destinationStr,
          mode = mode,
          apiKey = apiKey,
          waypoints = waypointsStr,
          onSuccess = { directionsResponse ->
            // Use extractRouteDetails to extract the route details
            val routeDetails = extractRouteDetails(directionsResponse, origin, destination)

            if (routeDetails != null) {
              // Update the appropriate StateFlow
              _activityRouteDetails.value = routeDetails
            } else {
              Log.e("DirectionsViewModel", "Failed to extract route details")
            }
          },
          onFailure = { exception ->
            Log.e("DirectionsViewModel", "Failed to fetch directions", exception)
          })
    }
  }

  /**
   * Helper function to extract RouteDetails from the DirectionsResponse.
   *
   * @param directionsResponse The response from the Directions API.
   * @param origin The starting point of the route as a LatLng object.
   * @param destination The ending point of the route as a LatLng object.
   * @return RouteDetails object containing the overview path and leg paths, or null if an error
   *   occurs.
   */
  private fun extractRouteDetails(
      directionsResponse: DirectionsResponse,
      origin: LatLng,
      destination: LatLng
  ): RouteDetails? {
    return try {
      // Extract only the first route from the response
      val route = directionsResponse.routes.firstOrNull()
      if (route != null) {
        // Decode the overview polyline for the entire route
        val overviewPath = PolyUtil.decode(route.overviewPolyline.points)

        // Decode the polyline for each leg and add it to the legs list
        val legsPaths = route.legs.map { leg -> PolyUtil.decode(leg.overviewPolyline.points) }

        // Create and return a RouteDetails object
        RouteDetails(
            origin = origin, destination = destination, route = overviewPath, legsRoute = legsPaths)
      } else {
        Log.e("DirectionsHelper", "No routes found in directions response")
        null
      }
    } catch (e: Exception) {
      Log.e("DirectionsHelper", "Error extracting route details from directions response", e)
      null
    }
  }
}
