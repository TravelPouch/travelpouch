package com.github.se.travelpouch.model.activity.map

/** Interface for the Directions Repository. */
interface DirectionsRepositoryInterface {

  /**
   * Fetches directions from the Google Maps Directions API.
   *
   * @param origin The starting point of the route, formatted as "latitude,longitude".
   * @param destination The ending point of the route, formatted as "latitude,longitude".
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   * @param apiKey The API key used to authenticate the request.
   * @param waypoints A string of waypoints formatted as "latitude,longitude|latitude,longitude"
   *   (optional).
   * @param onSuccess Callback that is called when the request is successful.
   * @param onFailure Callback that is called when the request fails.
   */
  fun getDirections(
      origin: String,
      destination: String,
      mode: String,
      waypoints: String? = null,
      onSuccess: (DirectionsResponse) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
