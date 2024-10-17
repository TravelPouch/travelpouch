package com.github.se.travelpouch.model.location

import android.util.Log
import com.github.se.travelpouch.model.Location
import com.google.firebase.Timestamp
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray

/** Repository for searching locations using the Nominatim API. */
class NominatimLocationRepository(val client: OkHttpClient) : LocationRepository {

  /**
   * Parses the JSON response from the Nominatim API.
   *
   * @param body The JSON response from the Nominatim API.
   * @return A list of locations.
   */
  private fun parseBody(body: String): List<Location> {
    val jsonArray = JSONArray(body)

    return List(jsonArray.length()) { i ->
      val jsonObject = jsonArray.getJSONObject(i)
      val lat = jsonObject.getDouble("lat")
      val lon = jsonObject.getDouble("lon")
      val name = jsonObject.getString("display_name")
      Location(lat, lon, Timestamp.now(), name)
    }
  }

  /**
   * Search for locations based on a query.
   *
   * @param query The query to search for.
   * @param onSuccess Callback that is called when the search is successful.
   * @param onFailure Callback that is called when the search fails.
   */
  override fun search(
      query: String,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Using HttpUrl.Builder to properly construct the URL with query parameters.
    val url =
        HttpUrl.Builder()
            .scheme("https")
            .host("nominatim.openstreetmap.org")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .addQueryParameter("format", "json")
            .build()

    // Create the request with a custom User-Agent and optional Referer
    val request =
        Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "TravelPouchApp/1.0 (travelpouchswent@gmail.com)") // Set a proper User-Agent
            .build()
    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                Log.e("NominatimLocationRepository", "Failed to execute request", e)
                onFailure(e)
              }

              override fun onResponse(call: Call, response: Response) {
                response.use {
                  if (!response.isSuccessful) {
                    onFailure(Exception("Unexpected code $response"))
                    Log.d("NominatimLocationRepository", "Unexpected code $response")
                    return
                  }

                  val body = response.body?.string()
                  if (body != null) {
                    onSuccess(parseBody(body))
                    Log.d("NominatimLocationRepository", "Body: $body")
                  } else {
                    Log.d("NominatimLocationRepository", "Empty body")
                    onSuccess(emptyList())
                  }
                }
              }
            })
  }
}
