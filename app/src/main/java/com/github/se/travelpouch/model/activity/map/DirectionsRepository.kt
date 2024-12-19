// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.model.activity.map

import android.annotation.SuppressLint
import android.util.Log
import com.github.se.travelpouch.helper.NetworkManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Repository for fetching directions using the Google Maps Directions API. */
class DirectionsRepository(client: OkHttpClient, private val apiKey: String) :
    DirectionsRepositoryInterface {

  private val networkManager: NetworkManager = NetworkManager(client)

  /**
   * Parses the JSON response from the Google Maps Directions API.
   *
   * @param body The JSON response from the API.
   * @return A DirectionsResponse object containing the parsed data.
   */
  private fun parseBody(body: String): DirectionsResponse {
    val jsonObject = JSONObject(body)

    // Check the status field
    val status = jsonObject.getString("status")
    if (status != "OK") {
      throw Exception("API request failed with status: $status")
    }

    val routesArray = jsonObject.getJSONArray("routes")

    // Parse routes using Kotlin's functional style
    val routes =
        List(routesArray.length()) { i ->
          val routeObject = routesArray.getJSONObject(i)

          // Extract overview polyline for the route
          val overviewPolylineObject = routeObject.getJSONObject("overview_polyline")
          val points = overviewPolylineObject.getString("points")

          // Extract the legs array using optJSONArray to avoid JSONException
          val legsArray = routeObject.optJSONArray("legs")

          // Check if legsArray is null or empty
          val legs =
              if (legsArray != null && legsArray.length() > 0) {
                List(legsArray.length()) { j ->
                  val legObject = legsArray.getJSONObject(j)

                  // Extract distance and duration information for each leg
                  val distanceObject = legObject.getJSONObject("distance")
                  val distanceText = distanceObject.getString("text")
                  val distanceValue = distanceObject.getInt("value") // in meters

                  val durationObject = legObject.getJSONObject("duration")
                  val durationText = durationObject.getString("text")
                  val durationValue = durationObject.getInt("value") // in seconds

                  // Extract start and end addresses
                  val startAddress = legObject.getString("start_address")
                  val endAddress = legObject.getString("end_address")

                  // Extract start and end locations
                  val startLocationObject = legObject.getJSONObject("start_location")
                  val startLat = startLocationObject.getDouble("lat")
                  val startLng = startLocationObject.getDouble("lng")
                  val startLocation = LatLng(startLat, startLng)

                  val endLocationObject = legObject.getJSONObject("end_location")
                  val endLat = endLocationObject.getDouble("lat")
                  val endLng = endLocationObject.getDouble("lng")
                  val endLocation = LatLng(endLat, endLng)

                  // Extract and decode the polyline for each step in the leg
                  val stepsArray = legObject.getJSONArray("steps")
                  val legLatLngPoints = mutableListOf<LatLng>()

                  List(stepsArray.length()) { k ->
                    val stepObject = stepsArray.getJSONObject(k)

                    val stepPolylineObject = stepObject.getJSONObject("polyline")

                    val stepPoints = PolyUtil.decode(stepPolylineObject.getString("points"))

                    legLatLngPoints.addAll(stepPoints)
                  }

                  // Encode the complete leg points back to a polyline string
                  val legPointsEncoded = PolyUtil.encode(legLatLngPoints)

                  // Create and return a Leg object
                  Leg(
                      distanceText = distanceText,
                      distanceValue = distanceValue,
                      durationText = durationText,
                      durationValue = durationValue,
                      startAddress = startAddress,
                      endAddress = endAddress,
                      startLocation = startLocation,
                      endLocation = endLocation,
                      overviewPolyline = OverviewPolyline(legPointsEncoded))
                }
              } else {
                emptyList() // Return empty list
              }

          // Create and return a Route object including the overview polyline and legs
          Route(overviewPolyline = OverviewPolyline(points), legs = legs)
        }

    return DirectionsResponse(routes)
  }

  /**
   * Fetches directions from the Google Maps Directions API.
   *
   * @param origin The starting point of the route, formatted as "latitude,longitude".
   * @param destination The ending point of the route, formatted as "latitude,longitude".
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   * @param waypoints A string of waypoints formatted as "latitude,longitude|latitude,longitude"
   *   (optional).
   * @param onSuccess Callback that is called when the request is successful.
   * @param onFailure Callback that is called when the request fails.
   */
  @SuppressLint("SuspiciousIndentation")
  override fun getDirections(
      origin: String,
      destination: String,
      mode: String,
      waypoints: String?,
      onSuccess: (DirectionsResponse) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Construct the URL with query parameters using HttpUrl.Builder
    val urlBuilder =
        HttpUrl.Builder()
            .scheme("https")
            .host("maps.googleapis.com")
            .addPathSegment("maps")
            .addPathSegment("api")
            .addPathSegment("directions")
            .addPathSegment("json")
            .addQueryParameter("origin", origin)
            .addQueryParameter("destination", destination)
            .addQueryParameter("mode", mode)
            .addQueryParameter("key", apiKey)

    // Add waypoints if provided
    if (!waypoints.isNullOrEmpty()) {
      urlBuilder.addQueryParameter("waypoints", waypoints)
    }

    // Build the URL
    val url = urlBuilder.build()

    // Create the request
    val request =
        Request.Builder()
            .url(url)
            .header("User-Agent", "TravelPouchApp/1.0 (travelpouchswent@gmail.com)")
            .build()

    // Use NetworkManager to make the network call
    networkManager.executeRequest(
        request,
        onSuccess = { body ->
          try {

            val directionsResponse = parseBody(body)
            onSuccess(directionsResponse)
          } catch (e: Exception) {
            Log.e("DirectionsRepository", "Failed to parse response", e)
            onFailure(e)
          }
        },
        onFailure = { e ->
          Log.e("DirectionsRepository", "Network request failed", e)
          onFailure(e)
        })
  }
}
