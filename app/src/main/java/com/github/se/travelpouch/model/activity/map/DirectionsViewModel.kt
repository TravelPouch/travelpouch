package com.github.se.travelpouch.model.activity.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/** ViewModel for fetching and managing directions data from the Google Maps Directions API. */
class DirectionsViewModel(private val repository: DirectionsRepositoryInterface) : ViewModel() {

    // StateFlow to hold the list of lists of LatLng points for each route
    private val _pathPoints = MutableStateFlow<List<List<LatLng>>>(emptyList())
    val pathPoints: StateFlow<List<List<LatLng>>>
        get() = _pathPoints

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

                    val points = extractPathPoints(directionsResponse)
                    _pathPoints.value = points
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
        val allRoutesPoints = mutableListOf<List<LatLng>>()

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
}
