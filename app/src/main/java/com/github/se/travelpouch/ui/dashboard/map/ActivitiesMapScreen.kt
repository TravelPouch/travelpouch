package com.github.se.travelpouch.ui.dashboard.map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.activity.map.DirectionsViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function that displays a map screen showing all activities. The activities are marked
 * on the map and the initial camera position is determined based on the first activity's location.
 *
 * @param activityViewModel The ViewModel containing the list of activities.
 * @param navigationActions Navigation actions for managing app navigation.
 */
@Composable
fun ActivitiesMapScreen(
    activityViewModel: ActivityViewModel,
    navigationActions: NavigationActions,
    directionsViewModel: DirectionsViewModel
) {

  // Collect the list of activities from the ViewModel
  val listOfActivities by activityViewModel.activities.collectAsState()

  val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

  // Default location to use if activities are not yet loaded (e.g., Paris)
  val defaultLocation = LatLng(48.8566, 2.3522)

  // State to track the camera position, initially set to the default location
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
  }

  // Collect the path points from the DirectionsViewModel
  val routeDetails by directionsViewModel.activityRouteDetails.collectAsState()

  var selectedRouteIndex by remember { mutableStateOf(0) } // Track selected route index

  // Use DisposableEffect to monitor when the screen is composed/destroyed
  LaunchedEffect(listOfActivities) {
    // Fetch directions if the list of activities changes
    directionsViewModel.fetchDirectionsForActivities(listOfActivities, "walking")
  }

  // Effect that runs whenever the list of activities changes
  CameraUpdater(listOfActivities, cameraPositionState)

  Scaffold(
      modifier = Modifier.testTag("ActivityMapScreen"),
      content = { paddingValues ->

        // Use a Box to overlay the button on top of the map
        Box(modifier = Modifier.padding(paddingValues)) {
          // Display the Google Map with markers for each activity
          GoogleMap(
              modifier = Modifier.padding(paddingValues).testTag("Map"),
              cameraPositionState = cameraPositionState) {
                // Add a marker for each activity's location
                listOfActivities.forEachIndexed { index, activity ->
                  activity.location.let { location -> // Ensure location is not null
                    // Use the helper function to get the appropriate marker icon
                    val icon = getMarkerIcon(index, listOfActivities.size)

                    // Display the marker with the customized icon
                    Marker(
                        state =
                            rememberMarkerState(
                                position = LatLng(location.latitude, location.longitude)),
                        title = activity.title,
                        snippet = dateFormat.format(activity.date.toDate()),
                        icon = icon)
                  }
                }

                // Draw the walking path using the leg paths
                if (routeDetails != null && routeDetails!!.legsRoute.isNotEmpty()) {
                  Log.d("ActivitiesMapScreen", "Drawing leg polylines")

                  // Iterate over each leg path and draw the segment with different styles
                  routeDetails!!.legsRoute.forEachIndexed { index, legPath ->
                    val prop = (index.toDouble() / routeDetails!!.legsRoute.size.toDouble()) - 0.5

                    // Use the helper function to get the gradient color
                    val color =
                        getGradientColor(
                            index = index, totalSegments = routeDetails!!.legsRoute.size)

                    // Introduce a small offset by adjusting each point slightly based on index
                    val offsetLegPath =
                        legPath.map { point ->
                          LatLng(point.latitude + prop * 0.0002, point.longitude + prop * 0.00002)
                        }

                    val zIndex =
                        if (index == selectedRouteIndex) {
                          (routeDetails!!.legsRoute.size + 1).toFloat() // Selected route in blue
                        } else {
                          (index + 1).toFloat() // Other routes in gray
                        }

                    Polyline(
                        points = offsetLegPath,
                        clickable = true,
                        width = 20f,
                        color = color,
                        endCap = ButtCap(),
                        jointType = JointType.ROUND,
                        zIndex = zIndex,
                        onClick = { selectedRouteIndex = index } // Update selected route
                        )
                  }
                }
              }
          // Add a floating action button for going back
          FloatingActionButton(
              onClick = { navigationActions.goBack() },
              modifier =
                  Modifier.align(Alignment.TopStart) // Position the button at the top-left
                      .padding(16.dp)
                      .size(48.dp)
                      .testTag("GoBackButton")) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
              }
        }
      })
}

// Helper function to generate a consistent gradient color
fun getGradientColor(
    index: Int,
    totalSegments: Int,
    colorStart: Color = Color(0xFF03A9F4).copy(alpha = 0.7f), // Soft Blue with consistent alpha
    colorEnd: Color = Color(0xFF3F51B5).copy(alpha = 0.7f) // Soft Purple with the same alpha
): Color {
  // Function to interpolate between two colors
  fun interpolateColor(colorStart: Color, colorEnd: Color, fraction: Float): Color {
    val red = (colorStart.red + (colorEnd.red - colorStart.red) * fraction)
    val green = (colorStart.green + (colorEnd.green - colorStart.green) * fraction)
    val blue = (colorStart.blue + (colorEnd.blue - colorStart.blue) * fraction)
    return Color(red, green, blue, alpha = colorStart.alpha) // Keep alpha consistent
  }

  // Calculate the gradient color based on the index
  return if (totalSegments > 1) {
    val fraction = index.toFloat() / (totalSegments - 1)
    interpolateColor(colorStart, colorEnd, fraction)
  } else {
    colorStart
  }
}

/**
 * Helper function to get the appropriate marker icon based on the activity's index.
 *
 * @param index The index of the activity in the list.
 * @param totalActivities The total number of activities in the list.
 * @return The appropriate BitmapDescriptor for the marker icon.
 */
fun getMarkerIcon(index: Int, totalActivities: Int): BitmapDescriptor {
  return when {
    // Use a custom icon for the first activity
    index == 0 -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)

    // Use a custom icon for the last activity
    index == totalActivities - 1 ->
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
    // Replace with your icon resource

    // Use the default icon for all other activities
    else ->
        BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_CYAN) // You can choose a different hue if you like
  }
}
