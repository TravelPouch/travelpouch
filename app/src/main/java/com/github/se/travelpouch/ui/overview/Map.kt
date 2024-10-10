package com.github.se.travelpouch.ui.overview

// import com.github.se.bootcamp.model.todo.ListToDosViewModel
// import com.github.se.bootcamp.ui.navigation.BottomNavigationMenu
// import com.github.se.bootcamp.ui.navigation.LIST_TOP_LEVEL_DESTINATION
// import com.github.se.bootcamp.ui.navigation.NavigationActions
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen(
    navigationActions: NavigationActions? = null,
    travelContainers: List<TravelContainer>
) {
  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      //        bottomBar = {
      //            BottomNavigationMenu(
      //                onTabSelect = { route -> navigationActions.navigateTo(route) },
      //                tabList = LIST_TOP_LEVEL_DESTINATION,
      //                selectedItem = navigationActions.currentRoute())
      //        },
      content = { paddingValues ->
        MapContent(modifier = Modifier.padding(paddingValues), travelContainers = travelContainers)
      })
}

@Composable
fun MapContent(modifier: Modifier = Modifier, travelContainers: List<TravelContainer>) {
  val markers = travelContainers.filter { true }

  val cameraPositionState = rememberCameraPositionState {
    // Set initial camera position to the first TravelContainer's location or a default
    if (markers.isNotEmpty()) {
      val firstLocation = markers.first().location
      position =
          CameraPosition.fromLatLngZoom(
              LatLng(firstLocation.latitude, firstLocation.longitude), 10f)
    } else {
      position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
    }
  }

  GoogleMap(
      modifier = modifier.fillMaxSize().testTag("mapScreen"),
      cameraPositionState = cameraPositionState) {
        markers.forEach { travelContainer ->
          val location = travelContainer.location
          val position = LatLng(location.latitude, location.longitude)
          val markerState = rememberMarkerState(position = position)
          Marker(
              state = markerState,
              title = travelContainer.title,
              snippet = travelContainer.description)
        }
      }
}
