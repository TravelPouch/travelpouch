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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.activity.map.DirectionsViewModel
import com.github.se.travelpouch.model.activity.map.RouteDetails
import com.github.se.travelpouch.model.gps.GPSViewModel
import com.github.se.travelpouch.permissions.LocationPermissionComposable
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
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

  // Filter out activities with invalid locations (latitude and longitude are both 0.0)
  val validActivities =
      listOfActivities.filter { activity ->
        activity.location.latitude != 0.0 && activity.location.longitude != 0.0
      }

  // State to track the selected activity
  var selectedActivity by remember { mutableStateOf<Activity?>(null) }

  // Date format to display the activity date
  val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

  // Default location to use if activities are not yet loaded (e.g., Paris)
  val defaultLocation = LatLng(48.8566, 2.3522)

  // State to track the camera position, initially set to the default location
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
  }

  // Collect the path points from the DirectionsViewModel
  val activitiesRouteDetails by directionsViewModel.activitiesRouteDetails.collectAsState()
  // Collect the GPS route details for path between GPS and selected activity
  val gpsRouteDetails by directionsViewModel.gpsRouteDetails.collectAsState()

  // Track selected route index
  var selectedRouteIndex by remember { mutableIntStateOf(0) }

  val gpsViewModel: GPSViewModel = viewModel(factory = GPSViewModel.Factory(LocalContext.current))

  // Collect the current location from GPSViewModel as a Compose state
  val currentLocation by gpsViewModel.realTimeLocation.collectAsState()

  // Request location permission and start location updates when permission is granted
  LocationPermissionComposable(
      onPermissionGranted = {
        Log.d("ActivitiesMapScreen", "Location permission granted.")
        gpsViewModel.startRealTimeLocationUpdates()
      },
      onPermissionDenied = { Log.d("ActivitiesMapScreen", "Location permission denied.") })

  // Fetch directions for the valid activities
  LaunchedEffect(validActivities) {
    // Fetch directions if the list of activities changes
    directionsViewModel.fetchDirectionsForActivities(validActivities, "walking")
  }

  // Fetch directions for the selected activity and GPS location
  LaunchedEffect(selectedActivity) {
    directionsViewModel.fetchDirectionsForGps(currentLocation, selectedActivity, "walking")
  }

  // Update the camera position when the valid activities change
  CameraUpdater(validActivities, cameraPositionState)

  Scaffold(
      modifier = Modifier.testTag("ActivityMapScreen"),
      content = { paddingValues ->

        // Use a Box to overlay the button on top of the map
        Box(modifier = Modifier.padding(paddingValues)) {
          // Display the Google Map with markers for each activity
          GoogleMap(
              modifier = Modifier.padding(paddingValues).testTag("Map"),
              cameraPositionState = cameraPositionState) {
                AddCurrentLocationMarker(currentLocation)

                AddActivityMarkers(
                    activities = validActivities,
                    dateFormat = dateFormat,
                    selectedActivity = selectedActivity,
                    onActivitySelected = { selectedActivity = it })

                DrawActivitiesPaths(
                    activitiesRouteDetails = activitiesRouteDetails,
                    selectedRouteIndex = selectedRouteIndex,
                    onRouteSelected = { selectedRouteIndex = it })

                DrawGpsToActivityPath(gpsRouteDetails)
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

@Composable
fun AddCurrentLocationMarker(currentLocation: LatLng?) {
  Log.d("ActivitiesMapScreen", "Adding current location marker")

  currentLocation?.let { location ->
    Marker(
        state = MarkerState(position = location),
        title = "You are here",
        snippet = "Current location",
    )
  }
}

@Composable
fun AddActivityMarkers(
    activities: List<Activity>,
    dateFormat: SimpleDateFormat,
    selectedActivity: Activity?,
    onActivitySelected: (Activity?) -> Unit
) {
  Log.d("ActivitiesMapScreen", "Adding activity markers")
  activities.forEachIndexed { index, activity ->
    val icon = getMarkerIcon(index, activities.size)
    val location = LatLng(activity.location.latitude, activity.location.longitude)
    Marker(
        state = rememberMarkerState(position = location),
        title = activity.title,
        snippet = dateFormat.format(activity.date.toDate()),
        icon = icon,
        onClick = {
          onActivitySelected(if (selectedActivity == activity) null else activity)
          true
        })
  }
}

@Composable
fun DrawActivitiesPaths(
    activitiesRouteDetails: RouteDetails?,
    selectedRouteIndex: Int,
    onRouteSelected: (Int) -> Unit
) {
  if (activitiesRouteDetails != null && activitiesRouteDetails.legsRoute.isNotEmpty()) {
    Log.d("ActivitiesMapScreen", "Drawing activities paths")
    activitiesRouteDetails.legsRoute.forEachIndexed { index, legPath ->
      val prop = (index.toDouble() / activitiesRouteDetails.legsRoute.size.toDouble()) - 0.5

      // Use the helper function to get the gradient color
      val color =
          getGradientColor(index = index, totalSegments = activitiesRouteDetails.legsRoute.size)

      // Introduce a small offset by adjusting each point slightly based on index
      val offsetLegPath =
          legPath.map { point ->
            LatLng(point.latitude + prop * 0.00001, point.longitude + prop * 0.00001)
          }

      // Set the zIndex to ensure the selected route is always on top
      val zIndex =
          if (index == selectedRouteIndex) {
            (activitiesRouteDetails.legsRoute.size + 1).toFloat() // Selected route
          } else {
            (index + 1).toFloat() // Other routes
          }

      Polyline(
          points = offsetLegPath,
          clickable = true,
          width = 20f,
          color = color,
          endCap = ButtCap(),
          jointType = JointType.ROUND,
          zIndex = zIndex,
          onClick = { onRouteSelected(index) })
    }
  }
}

@Composable
fun DrawGpsToActivityPath(gpsRouteDetails: RouteDetails?) {
  Log.d("ActivitiesMapScreen", "Drawing GPS to activity path")
  gpsRouteDetails?.legsRoute?.forEach { points ->
    Polyline(points = points, color = Color.Blue, width = 10f)
  }
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
