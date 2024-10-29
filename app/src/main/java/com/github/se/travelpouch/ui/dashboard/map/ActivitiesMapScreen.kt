package com.github.se.travelpouch.ui.dashboard.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

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
    navigationActions: NavigationActions
) {

  // Collect the list of activities from the ViewModel
  val listOfActivities by activityViewModel.activities.collectAsState()

  // Default location to use if activities are not yet loaded (e.g., Paris)
  val defaultLocation = LatLng(48.8566, 2.3522)

  // State to track the camera position, initially set to the default location
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
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
                listOfActivities.forEach { activity ->
                  activity.location.let { location -> // Ensure location is not null
                    Marker(
                        state =
                            rememberMarkerState(
                                position =
                                    LatLng(
                                        location.latitude.toDouble(),
                                        location.longitude.toDouble())),
                        title = activity.title, // The title of the activity
                        snippet = activity.description, // The description of the activity
                        contentDescription = "Marker for ${activity.title}")
                  }
                }
              }

          // Add a floating action button for going back
          FloatingActionButton(
              onClick = { navigationActions.goBack() },
              modifier =
                  Modifier.align(Alignment.TopStart) // Position the button at the top-left
                      .padding(16.dp)
                      .size(48.dp) // Optional size for the button
                      .testTag("GoBackButton")) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
              }
        }
      })
}
