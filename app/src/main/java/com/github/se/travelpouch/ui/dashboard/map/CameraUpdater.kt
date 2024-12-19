// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.se.travelpouch.model.activity.Activity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

@Composable
fun CameraUpdater(listOfActivities: List<Activity>, cameraPositionState: CameraPositionState) {
  LaunchedEffect(listOfActivities) {
    if (listOfActivities.isNotEmpty()) {
      val firstLocation = listOfActivities.first().location
      cameraPositionState.position =
          CameraPosition.fromLatLngZoom(
              LatLng(firstLocation.latitude, firstLocation.longitude), 10f)
    }
  }
}
