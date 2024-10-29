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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.maps.android.compose.CameraPositionState

/**
 * Composable function that displays a map screen showing all activities. The activities are marked
 * on the map and the initial camera position is determined based on the first activity's location.
 *
 * @param activityViewModel The ViewModel containing the list of activities.
 * @param navigationActions Navigation actions for managing app navigation.
 */
@Composable
fun MapScaffold(
    listOfActivities: List<Activity>,
    cameraPositionState: CameraPositionState,
    navigationActions: NavigationActions
) {
  Scaffold(
      modifier = Modifier.testTag("ActivityMapScreen"),
      content = { paddingValues ->

        // Use a Box to overlay the button on top of the map
        Box(modifier = Modifier.padding(paddingValues)) {
          // Display the Google Map with markers for each activity
          Map(listOfActivities, paddingValues, cameraPositionState)

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
