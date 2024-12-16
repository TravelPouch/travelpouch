// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard.map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.BuildConfig
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.activity.map.DirectionsViewModel
import com.github.se.travelpouch.model.activity.map.RouteDetails
import com.github.se.travelpouch.model.gps.GPSViewModel
import com.github.se.travelpouch.permissions.LocationPermissionComposable
import com.github.se.travelpouch.ui.navigation.NavigationActions
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
import kotlinx.coroutines.launch

/**
 * Displays a map screen showing activities and their details.
 *
 * It shows a Google Map with markers for activities, paths between them, and a dynamic bottom sheet
 * to show detailed information for selected activities or routes and GPS location.
 *
 * @param activityViewModel The ViewModel containing the list of activities.
 * @param navigationActions Navigation actions for managing app navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesMapScreen(
    activityViewModel: ActivityViewModel,
    navigationActions: NavigationActions
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

  // Create a ViewModel for fetching directions between activities
  val directionsViewModel: DirectionsViewModel =
      viewModel(
          factory =
              DirectionsViewModel.provideFactory(
                  BuildConfig.MAPS_API_KEY) // Inject the API key for the DirectionsViewModel
          )

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

  val bottomSheetScaffoldState =
      rememberBottomSheetScaffoldState(
          bottomSheetState =
              rememberStandardBottomSheetState(
                  skipHiddenState = false // Ensure this is set to false
                  ))

  var bottomSheetContent by remember { mutableStateOf<@Composable () -> Unit>({}) }

  val scope = rememberCoroutineScope()

  BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      sheetPeekHeight = 0.dp,
      sheetContent = { Box(modifier = Modifier.fillMaxWidth()) { bottomSheetContent() } }) {
          innerPadding ->

        // Use a Box to overlay the button on top of the map
        Box(modifier = Modifier.padding(innerPadding)) {
          // Display the Google Map with markers for each activity
          GoogleMap(
              modifier = Modifier.fillMaxSize().testTag("Map"),
              cameraPositionState = cameraPositionState) {
                AddCurrentLocationMarker(currentLocation)

                AddActivityMarkers(
                    activities = validActivities,
                    dateFormat = dateFormat,
                    onActivitySelected = {
                      selectedActivity = it

                      bottomSheetContent = { ActivityDetailsContent(it) }
                      scope.launch { bottomSheetScaffoldState.bottomSheetState.expand() }
                    })

                DrawActivitiesPaths(
                    activitiesRouteDetails = activitiesRouteDetails,
                    selectedRouteIndex = selectedRouteIndex,
                    onRouteSelected = {
                      selectedRouteIndex = it

                      bottomSheetContent = {
                        LegDetailsContent(activitiesRouteDetails, it, validActivities)
                      }
                      scope.launch { bottomSheetScaffoldState.bottomSheetState.expand() }
                    })

                DrawGpsToActivityPath(
                    gpsRouteDetails = gpsRouteDetails,
                    onRouteSelected = {
                      bottomSheetContent = { GpsDetailsContent(gpsRouteDetails, selectedActivity) }
                      scope.launch { bottomSheetScaffoldState.bottomSheetState.expand() }
                    })
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
      }
}

// -------------------------------------------------
// Helper Functions for Displaying Information in Bottom Sheet
// -------------------------------------------------

/**
 * Displays detailed information about an activity, including:
 * - Title, date, and time.
 * - Location name and description.
 *
 * @param activity The activity to display.
 */
@Composable
fun ActivityDetailsContent(activity: Activity) {
  // Date formatter
  val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val formattedDate = dateFormatter.format(activity.date.toDate())

  // Time formatter
  val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
  val formattedTime = timeFormatter.format(activity.date.toDate())

  // Create a nested scroll connection
  val nestedScrollConnection = remember { object : NestedScrollConnection {} }

  LazyColumn(
      modifier =
          Modifier.fillMaxWidth()
              .fillMaxHeight(0.3F)
              .padding(16.dp)
              .nestedScroll(nestedScrollConnection) // Add nested scroll connection
      ) {
        // Title Section
        item {
          Text(
              text = activity.title,
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(bottom = 12.dp).testTag("ActivityTitle"))
        }
        // Divider
        item {
          Divider(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
              modifier = Modifier.padding(bottom = 12.dp))
        }
        // Date Row
        item {
          DetailRow(
              icon = Icons.Default.CalendarToday, text = formattedDate, testTag = "ActivityDate")
        }
        // Time Row
        item {
          DetailRow(icon = Icons.Default.AccessTime, text = formattedTime, testTag = "ActivityTime")
        }
        // Location Row
        item {
          DetailRow(
              icon = Icons.Default.LocationOn,
              text = activity.location.name,
              testTag = "ActivityLocation")
        }
        // Description Row
        item {
          DetailRow(
              icon = Icons.Default.Description,
              text = activity.description,
              testTag = "ActivityDescription")
        }
      }
}

/**
 * Displays details for a specific route leg between two activities. Shows the starting activity,
 * distance, duration, and the ending activity as a subtitle.
 *
 * @param routeDetails Route details containing distances and durations.
 * @param legIndex Index of the route leg to display.
 * @param activities List of activities to determine start and end points.
 */
@Composable
fun LegDetailsContent(routeDetails: RouteDetails?, legIndex: Int, activities: List<Activity>) {
  val startActivity = activities.getOrNull(legIndex)
  val endActivity = activities.getOrNull(legIndex + 1)
  val distance = routeDetails?.legsDistance?.getOrNull(legIndex) ?: "N/A"
  val duration = routeDetails?.legsDuration?.getOrNull(legIndex) ?: "N/A"

  RouteDetailsContent(
      title = startActivity?.title,
      distance = distance,
      duration = duration,
      subtitle = endActivity?.title,
      testTag = "Leg")
}

/**
 * Displays route details between the user's current location and a selected activity. Shows the
 * distance, duration, and the selected activity as a subtitle.
 *
 * @param gpsRouteDetails Route details from the user's GPS location to the activity.
 * @param selectedActivity The activity to which the route is displayed.
 */
@Composable
fun GpsDetailsContent(gpsRouteDetails: RouteDetails?, selectedActivity: Activity?) {
  val distance = gpsRouteDetails?.legsDistance?.firstOrNull() ?: "N/A"
  val duration = gpsRouteDetails?.legsDuration?.firstOrNull() ?: "N/A"

  RouteDetailsContent(
      title = "Your current Location",
      distance = distance,
      duration = duration,
      subtitle = selectedActivity?.title,
      testTag = "Gps")
}

/**
 * Displays route details in a formatted layout. Includes the title, distance, duration, and an
 * optional subtitle.
 *
 * @param title Title of the route (e.g., starting point).
 * @param distance The distance of the route leg.
 * @param duration The duration of the route leg.
 * @param subtitle Optional subtitle (e.g., destination point).
 * @param testTag A tag used for testing the composable.
 */
@Composable
fun RouteDetailsContent(
    title: String?,
    distance: String,
    duration: String,
    subtitle: String? = null,
    testTag: String = ""
) {
  Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
    // Title Section
    title?.let {
      Text(
          text = it,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 8.dp).testTag("RouteDetailsTitle_$testTag"))
    }

    // Row for Divider and Details
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Divider(
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
          modifier = Modifier.width(1.dp).height(60.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        DetailRow(
            icon = Icons.Default.LocationOn,
            text = "Distance: $distance",
            testTag = "RouteDetailsDistance_$testTag")
        DetailRow(
            icon = Icons.Default.AccessTime,
            text = "Duration: $duration",
            testTag = "RouteDetailsDuration$testTag")
      }
    }

    // Subtitle Section
    subtitle?.let {
      Text(
          text = it,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 8.dp).testTag("RouteDetailsSubtitle_$testTag"))
    }
  }
}

/**
 * Displays a row with an icon and a text label. Useful for showing detailed information with an
 * accompanying visual indicator.
 *
 * @param icon The icon to display on the left.
 * @param text The text content to display.
 * @param testTag A tag for identifying the composable during testing.
 */
@Composable
fun DetailRow(icon: ImageVector, text: String, testTag: String = "") {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(20.dp).testTag("${testTag}_Icon"))
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.testTag(testTag))
  }
}

// -------------------------------------------------
// Helper functions for drawing paths and markers
// -------------------------------------------------

/**
 * Adds a marker to the map representing the user's current location. The marker is displayed only
 * if the location is available.
 *
 * @param currentLocation The user's current location as a LatLng object.
 */
@Composable
fun AddCurrentLocationMarker(currentLocation: LatLng?) {
  Log.d("ActivitiesMapScreen", "Adding current location marker")

  currentLocation?.let { location ->
    Marker(
        state = MarkerState(position = location),
        title = "You are here",
        snippet = "Current location",
        tag = Modifier.testTag("CurrentLocationMarker"))
  }
}

/**
 * Adds markers for a list of activities on the map. Each marker is placed at the activity's
 * location, with a unique icon and title. Clicking a marker triggers a callback with the selected
 * activity.
 *
 * @param activities List of activities to display as markers.
 * @param dateFormat Formatter for displaying the activity date in the marker snippet.
 * @param onActivitySelected Callback triggered when a marker is clicked, providing the selected
 *   activity.
 */
@Composable
fun AddActivityMarkers(
    activities: List<Activity>,
    dateFormat: SimpleDateFormat,
    onActivitySelected: (Activity) -> Unit
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
        tag = Modifier.testTag("Marker_${index}"),
        onClick = {
          onActivitySelected(activity)
          true
        })
  }
}

/**
 * Draws paths on the map between activities using polylines. Each path segment is styled with a
 * gradient color and can be clicked to select a specific route.
 *
 * @param activitiesRouteDetails The route details containing path points between activities.
 * @param selectedRouteIndex The index of the currently selected route, which is highlighted.
 * @param onRouteSelected Callback triggered when a route segment is clicked, providing the index of
 *   the selected route.
 */
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

/**
 * Draws a path on the map from the user's current GPS location to a selected activity. The path is
 * represented as a polyline and is clickable to trigger a callback.
 *
 * @param gpsRouteDetails The route details containing the path points from GPS to the activity.
 * @param onRouteSelected Callback invoked when the path is clicked.
 */
@Composable
fun DrawGpsToActivityPath(gpsRouteDetails: RouteDetails?, onRouteSelected: () -> Unit) {
  Log.d("ActivitiesMapScreen", "Drawing GPS to activity path")
  gpsRouteDetails?.legsRoute?.forEach { points ->
    Polyline(
        points = points,
        clickable = true,
        color = Color.Blue,
        width = 10f,
        onClick = { onRouteSelected() })
  }
}

// -------------------------------------------------
// Helper Functions for Map Customization
// -------------------------------------------------

/**
 * Generates a gradient color for a specific segment of a path. Interpolates between a start and end
 * color based on the segment's index in the total path.
 *
 * @param index The index of the current segment in the path.
 * @param totalSegments The total number of segments in the path.
 * @param colorStart The starting color of the gradient (default: soft blue).
 * @param colorEnd The ending color of the gradient (default: soft purple).
 * @return The calculated gradient color for the given segment.
 */
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
