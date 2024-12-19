// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
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
    val origin: LatLng, // Origin location
    val destination: LatLng, // Destination location
    val route: Path, // List of path for the entire route
    val legsRoute:
        List<Path>, // List of paths for each leg of the route. Each leg is a segment between
    val legsDistance: List<String>, // List of distances for each leg
    val legsDuration: List<String> // List of durations for each leg
) {
  companion object {
    val EMPTY =
        RouteDetails(
            origin = LatLng(0.0, 0.0),
            destination = LatLng(0.0, 0.0),
            route = emptyList(),
            legsRoute = emptyList(),
            legsDistance = emptyList(),
            legsDuration = emptyList())
  }
}

/**
 * ViewModel for fetching directions between activities using the Google Maps Directions API.
 *
 * @param repository The repository to fetch directions from.
 */
class DirectionsViewModel(private val repository: DirectionsRepositoryInterface) : ViewModel() {

  // StateFlow to hold the fetched route details for activities
  private val _activityRouteDetails = MutableStateFlow<RouteDetails?>(RouteDetails.EMPTY)
  val activitiesRouteDetails: StateFlow<RouteDetails?>
    get() = _activityRouteDetails

  // StateFlow to hold the fetched route details for activities
  private val _gpsRouteDetails = MutableStateFlow<RouteDetails?>(RouteDetails.EMPTY)
  val gpsRouteDetails: StateFlow<RouteDetails?>
    get() = _gpsRouteDetails

  /** Factory class for creating DirectionsViewModel instances. */
  // create factory
  companion object {
    fun provideFactory(apiKey: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DirectionsViewModel(
                DirectionsRepository(
                    client = OkHttpClient(), apiKey = apiKey // Inject API key dynamically
                    ))
                as T
          }
        }
  }

  /**
   * Fetches directions for the user's location and a selected activity.
   *
   * @param gpsLocation The location of the user.
   * @param selectedActivity The activity selected by the user.
   */
  fun fetchDirectionsForGps(gpsLocation: LatLng?, selectedActivity: Activity?, mode: String) {

    if (gpsLocation == null || selectedActivity == null) {
      Log.d("DirectionsViewModel", "Null location or activity")
      return
    }
    val selectedActivityLocation =
        LatLng(selectedActivity.location.latitude, selectedActivity.location.longitude)

    fetchDirections(_gpsRouteDetails, gpsLocation, selectedActivityLocation, mode)
  }

  /**
   * Fetches directions for a list of activities using the specified travel mode.
   *
   * @param activities The list of activities to create a route for.
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   */
  fun fetchDirectionsForActivities(activities: List<Activity>, mode: String) {
    if (activities.size < 2) {
      Log.d("DirectionsViewModel", "Not enough activities to create a route")
      return
    }

    // Extract the origin from the first activity
    val firstLocation = activities.firstOrNull()?.location
    if (firstLocation == null) {
      Log.d("DirectionsViewModel", "First activity has no location")
      return
    }
    val origin = LatLng(firstLocation.latitude, firstLocation.longitude)

    // Extract the destination from the last activity
    val lastLocation = activities.lastOrNull()?.location
    if (lastLocation == null) {
      Log.d("DirectionsViewModel", "Last activity has no location")
      return
    }
    val destination = LatLng(lastLocation.latitude, lastLocation.longitude)

    // Extract the waypoints, skipping activities with null location
    val waypoints =
        activities.subList(1, activities.size - 1).mapNotNull {
          it.location?.let { loc -> LatLng(loc.latitude, loc.longitude) }
        }

    // Fetch directions using the extracted origin, destination, and waypoints
    fetchDirections(_activityRouteDetails, origin, destination, mode, waypoints)
  }

  /**
   * Fetches directions from the repository and decodes the polyline points.
   *
   * @param origin The starting point of the route as a LatLng object.
   * @param destination The ending point of the route as a LatLng object.
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   */
  private fun fetchDirections(
      targetStateFlow: MutableStateFlow<RouteDetails?>,
      origin: LatLng,
      destination: LatLng,
      mode: String,
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
          waypoints = waypointsStr,
          onSuccess = { directionsResponse ->
            // Use extractRouteDetails to extract the route details
            val routeDetails = extractRouteDetails(directionsResponse, origin, destination)

            if (routeDetails != null) {
              // Update the appropriate StateFlow
              targetStateFlow.value = routeDetails
            } else {
              Log.e("DirectionsViewModel", "Failed to extract route details")
              targetStateFlow.value = RouteDetails.EMPTY
            }
          },
          onFailure = { exception ->
            Log.e("DirectionsViewModel", "Failed to fetch directions", exception)
            targetStateFlow.value = RouteDetails.EMPTY
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
        val legsDistance = route.legs.map { leg -> leg.distanceText }
        val legsDuration = route.legs.map { leg -> leg.durationText }

        // Create and return a RouteDetails object
        RouteDetails(
            origin = origin,
            destination = destination,
            route = overviewPath,
            legsRoute = legsPaths,
            legsDistance = legsDistance,
            legsDuration = legsDuration)
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
