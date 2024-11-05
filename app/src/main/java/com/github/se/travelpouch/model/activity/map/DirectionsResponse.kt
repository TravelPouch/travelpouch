package com.github.se.travelpouch.model.activity.map

/**
 * Data model for the entire response from the Google Maps Directions API. It contains a list of
 * routes, each representing a possible path from the origin to the destination.
 */
data class DirectionsResponse(
    val routes: List<Route> // List of routes returned by the API
)

/**
 * Data model for a single route within the Directions API response. Each route contains an overview
 * polyline, which is used to draw the route on the map.
 */
data class Route(
    val overviewPolyline: OverviewPolyline // The polyline representing the entire route
)

/**
 * Data model for the polyline object within a route. The polyline is encoded as a string and needs
 * to be decoded to a list of LatLng points.
 */
data class OverviewPolyline(
    val points: String // Encoded string of points that defines the path
)
