// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.travels.TravelContainer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Composable function that displays the map screen with a list of travel containers.
 *
 * @param travelContainers List of travel containers to be displayed on the map.
 */
@Composable
fun MapScreen(travelContainers: List<TravelContainer>) {
  Scaffold(
      modifier = Modifier.testTag("listTravelScreen"),
      content = { paddingValues ->
        MapContent(modifier = Modifier.padding(paddingValues), travelContainers = travelContainers)
      })
}

/**
 * Composable function that displays the map content with markers for each travel container.
 *
 * @param modifier Modifier for customizing the layout.
 * @param travelContainers List of travel containers to be displayed as markers on the map.
 * @param onInfoWindowClickCallback Callback function to be invoked when a marker info window is
 *   clicked.
 */
@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    travelContainers: List<TravelContainer>,
    onInfoWindowClickCallback: (TravelContainer) -> Unit = {}
) {

  val markers = travelContainers.filter { true }
  val context: Context = LocalContext.current
  val cameraPositionState = rememberCameraPositionState {
    // Set initial camera position to the first TravelContainer's location or a default
    if (markers.isNotEmpty()) {
      val firstLocation = markers.first().location
      position =
          CameraPosition.fromLatLngZoom(
              LatLng(firstLocation.latitude, firstLocation.longitude), 10f)
    } else {
      position = CameraPosition.fromLatLngZoom(LatLng(46.520564452328664, 6.567825512303322), 15f)
    }
  }

  LaunchedEffect(travelContainers) {
    if (travelContainers.isNotEmpty()) {
      val firstLocation = travelContainers.first().location
      cameraPositionState.position =
          CameraPosition.fromLatLngZoom(
              LatLng(firstLocation.latitude, firstLocation.longitude), 10f)
    }
  }

  GoogleMap(
      modifier = modifier.fillMaxSize().testTag("mapScreen"),
      cameraPositionState = cameraPositionState) {
        markers.forEach { travelContainer ->
          val location = travelContainer.location
          val position = LatLng(location.latitude, location.longitude)
          ClickableSimpleMarker(
              position = position,
              title = travelContainer.title,
              snippet = travelContainer.description,
              onInfoWindowClick = { marker ->
                val foundTravelContainer = markers.find { it.fsUid == marker.tag }
                if (foundTravelContainer != null) {
                  Log.d("MapScreen", "Travel container clicked: ${foundTravelContainer.title}")
                  onInfoWindowClickCallback(foundTravelContainer)
                } else {
                  Log.e("MapScreen", "No travel container found for marker tag: ${marker.tag}")
                  Toast.makeText(
                          context,
                          "No travel container found for marker tag: ${marker.tag}",
                          Toast.LENGTH_LONG)
                      .show()
                }
              },
              tag = travelContainer.fsUid)
        }
      }
}

// credit to https://dev.to/bubenheimer/effective-map-composables-non-draggable-markers-2b2

/**
 * Composable function that displays a clickable marker on the map.
 *
 * @param position The position of the marker.
 * @param title Optional title for the marker.
 * @param snippet Optional snippet for the marker.
 * @param onInfoWindowClick A lambda function to be called when the info window is clicked.
 * @param tag Optional tag to associate with the marker.
 */
@Composable
fun ClickableSimpleMarker(
    position: LatLng,
    title: String? = null,
    snippet: String? = null,
    onInfoWindowClick: (Marker) -> Unit = {},
    tag: Any? = null
) {
  val state = rememberUpdatedMarkerState(position)
  Marker(
      state = state,
      title = title,
      snippet = snippet,
      onInfoWindowClick = onInfoWindowClick,
      tag = tag)
}

/**
 * Composable function that remembers and updates the marker state.
 *
 * @param newPosition The new position of the marker.
 * @return The updated marker state.
 */
@Composable
fun rememberUpdatedMarkerState(newPosition: LatLng) =
    remember { MarkerState(position = newPosition) }.apply { position = newPosition }
