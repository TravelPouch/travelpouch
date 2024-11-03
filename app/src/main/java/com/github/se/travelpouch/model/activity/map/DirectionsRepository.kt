package com.github.se.travelpouch.model.activity.map

import android.util.Log
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

/** Repository for fetching directions using the Google Maps Directions API. */
class DirectionsRepository(private val client: OkHttpClient) : DirectionsRepositoryInterface {

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

    val routes =
        List(routesArray.length()) { i ->
          val routeObject = routesArray.getJSONObject(i)
          val overviewPolylineObject = routeObject.getJSONObject("overview_polyline")
          val points = overviewPolylineObject.getString("points")
          Route(OverviewPolyline(points))
        }

    return DirectionsResponse(routes)
  }

  /**
   * Fetches directions from the Google Maps Directions API.
   *
   * @param origin The starting point of the route, formatted as "latitude,longitude".
   * @param destination The ending point of the route, formatted as "latitude,longitude".
   * @param mode The travel mode ("driving", "walking", "bicycling", or "transit").
   * @param apiKey The API key used to authenticate the request.
   * @param onSuccess Callback that is called when the request is successful.
   * @param onFailure Callback that is called when the request fails.
   */
  override fun getDirections(
      origin: String,
      destination: String,
      mode: String,
      apiKey: String,
      onSuccess: (DirectionsResponse) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Construct the URL with query parameters using HttpUrl.Builder
    val url =
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
            .build()

    // Create the request
    val request =
        Request.Builder()
            .url(url)
            .header("User-Agent", "TravelPouchApp/1.0 (travelpouchswent@gmail.com)")
            .build()

    // Make the network call
    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                Log.e("DirectionsRepositoryImpl", "Failed to execute request", e)
                onFailure(e)
              }

              override fun onResponse(call: Call, response: Response) {
                response.use {
                  if (!response.isSuccessful) {
                    onFailure(Exception("Unexpected code $response"))
                    Log.d("DirectionsRepository", "Unexpected code $response")
                    return
                  }

                  val body = response.body?.string()
                  if (body != null) {
                    try {
                      // Parse the JSON response using Gson
                      val directionsResponse = parseBody(body)
                      onSuccess(directionsResponse)
                    } catch (e: Exception) {
                      onFailure(e)
                      Log.e("DirectionsRepository", "Failed to parse response", e)
                    }
                  } else {
                    Log.d("DirectionsRepository", "Empty body")
                    onFailure(Exception("Empty response body"))
                  }
                }
              }
            })
  }
}
