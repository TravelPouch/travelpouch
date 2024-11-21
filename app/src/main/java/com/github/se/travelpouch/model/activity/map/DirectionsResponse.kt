package com.github.se.travelpouch.model.activity.map

import com.google.android.gms.maps.model.LatLng

/**
 * Data model for the entire response from the Google Maps Directions API. It contains a list of
 * routes, each representing a possible path from the origin to the destination.
 */
data class DirectionsResponse(
    val routes: List<Route> // List of routes returned by the API
)

/**
 * Data model for a single route within the Directions API response. Each route contains an overview
 * polyline, which is used to draw the route on the map, and a list of legs, each representing a
 * segment of the journey between waypoints.
 */
data class Route(
    val overviewPolyline: OverviewPolyline, // The polyline representing the entire route
    val legs:
        List<Leg> // List of legs in the route, each leg represents a segment between waypoints
)

/**
 * Data model for a single leg within a route. A leg represents the journey between two waypoints,
 * and includes information such as distance, duration, start and end locations, addresses, and an
 * encoded polyline.
 */
data class Leg(
    val distanceText: String, // Human-readable representation of the distance (e.g., "3.4 km")
    val distanceValue: Int, // Distance in meters
    val durationText: String, // Human-readable representation of the duration (e.g., "15 mins")
    val durationValue: Int, // Duration in seconds
    val startAddress: String, // Human-readable address of the start location
    val endAddress: String, // Human-readable address of the end location
    val startLocation: LatLng, // LatLng representing the start point of the leg
    val endLocation: LatLng, // LatLng representing the end point of the leg
    val overviewPolyline: OverviewPolyline // Encoded polyline representing the path for this leg
) {
  init {
    require(distanceValue >= 0) { "distanceValue must be non-negative" }
    require(durationValue >= 0) { "durationValue must be non-negative" }
    require(startLocation.latitude in -90.0..90.0) { "Invalid latitude for startLocation" }
    require(startLocation.longitude in -180.0..180.0) { "Invalid longitude for startLocation" }
    require(endLocation.latitude in -90.0..90.0) { "Invalid latitude for endLocation" }
    require(endLocation.longitude in -180.0..180.0) { "Invalid longitude for endLocation" }
  }
}

/**
 * Data model for the polyline object within a route. The polyline is encoded as a string and needs
 * to be decoded to a list of LatLng points.
 */
data class OverviewPolyline(
    val points: String // Encoded string of points that defines the path
)
