package com.github.se.travelpouch.ui.dashboard.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.activity.Activity
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun Map(
    listOfActivities: List<Activity>,
    paddingValues: PaddingValues,
    cameraPositionState: CameraPositionState
) {
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
                            LatLng(location.latitude.toDouble(), location.longitude.toDouble())),
                title = activity.title, // The title of the activity
                snippet = activity.description, // The description of the activity
                contentDescription = "Marker for ${activity.title}")
          }
        }
      }
}
