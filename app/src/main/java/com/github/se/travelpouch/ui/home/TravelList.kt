package com.github.se.travelpouch.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations
import java.util.Locale

/**
 * Composable function for the travels list screen.
 *
 * @param navigationActions Actions for navigation.
 * @param listTravelViewModel List of travels as a viewmodel, to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TravelListScreen(
    navigationActions: NavigationActions,
    listTravelViewModel: ListTravelViewModel,
    activityViewModel: ActivityViewModel,
    eventViewModel: EventViewModel,
    documentViewModel: DocumentViewModel,
    profileModelView: ProfileModelView
) {
  // Fetch travels when the screen is launched
  LaunchedEffect(Unit) {
    listTravelViewModel.getTravels()
    profileModelView.getProfile()
  }

  val travelList = listTravelViewModel.travels.collectAsState()
  val currentProfile = profileModelView.profile.collectAsState()
  val isLoading = listTravelViewModel.isLoading.collectAsState()

  // Used for the screen orientation redraw
  val configuration = LocalConfiguration.current
  val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
  val mapHeight = if (isPortrait) 500.dp else 200.dp

  Scaffold(
      modifier = Modifier.testTag("TravelListScreen"),
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationActions.navigateTo(Screen.ADD_TRAVEL) },
            modifier = Modifier.testTag("createTravelFab")) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            tabList = listOf(TopLevelDestinations.NOTIFICATION, TopLevelDestinations.TRAVELS),
            navigationActions = navigationActions)
      },
      content = { pd ->
        Column(modifier = Modifier.fillMaxSize().padding(pd)) {
          // Map placed outside the LazyColumn to prevent it from being part of the scrollable
          ResizableStowableMapWithGoogleMap(mapHeight, travelList)

          LazyColumn(
              modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
              contentPadding = PaddingValues(bottom = 80.dp)) {
                if (travelList.value.isNotEmpty()) {
                  items(travelList.value.size) { index ->
                    TravelItem(travelContainer = travelList.value[index]) {
                      val travelId = travelList.value[index].fsUid
                      listTravelViewModel.selectTravel(travelList.value[index])
                      navigationActions.navigateTo(Screen.SWIPER)
                      eventViewModel.setIdTravel(travelId)
                      activityViewModel.setIdTravel(travelId)
                      documentViewModel.setIdTravel(travelId)
                    }
                  }
                } else {
                  item {
                    Row(
                        modifier =
                            Modifier.fillParentMaxSize()
                                .padding(top = 32.dp, start = 16.dp, end = 0.dp)
                                .padding(pd),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top) {
                          AnimatedVisibility(visible = isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.testTag("loadingSpinner").size(100.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 5.dp,
                                strokeCap = StrokeCap.Round,
                            )
                          }

                          Spacer(modifier = Modifier.fillMaxWidth(0.1f))

                          Text(
                              modifier = Modifier.testTag("emptyTravelPrompt"),
                              text = "You have no travels yet.",
                              style =
                                  MaterialTheme.typography.bodyLarge.copy(
                                      fontWeight = FontWeight.Bold),
                              color = MaterialTheme.colorScheme.onBackground)
                        }
                  }
                }
              }
        }
      })
}

/**
 * Composable function for displaying a travel item.
 *
 * @param travelContainer The travel container to display.
 * @param onClick Callback function for item click.
 */
@Composable
fun TravelItem(travelContainer: TravelContainer, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.testTag("travelListItem")
              .fillMaxWidth()
              .padding(vertical = 6.dp)
              .clickable(onClick = onClick),
      colors =
          CardColors(
              containerColor = MaterialTheme.colorScheme.surface,
              disabledContentColor = MaterialTheme.colorScheme.inverseSurface,
              contentColor = MaterialTheme.colorScheme.onSurface,
              disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
          )) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          // Date and Title Row
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(travelContainer.startTime.toDate()),
                    style = MaterialTheme.typography.bodySmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      text = travelContainer.title,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Bold)
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null)
                }
              }

          Spacer(modifier = Modifier.height(4.dp))

          // Description
          Text(text = travelContainer.description, style = MaterialTheme.typography.bodyMedium)

          // Location Name
          Text(
              text = travelContainer.location.name,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Light)
        }
      }
}

// inspired from :
// https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling
@Composable
fun ResizableStowableMapWithGoogleMap(
    maxMapHeightDp: Dp = 300.dp,
    travelList: State<List<TravelContainer>>
) {

  // State to track the height of the map
  val minHeightPx = 100f // Min height of the map (collapsed state)
  val latchDp = 30.dp // Height of the strap handle
  val maxHeightPx = maxMapHeightDp.value - latchDp.value // Max height of the map
  val mapHeight = rememberSaveable {
    mutableFloatStateOf(maxHeightPx)
  } // Initial height of the map in pixels
  val density = LocalDensity.current // Get the density scale factor

  // Track whether the map is collapsed (height passed 0 at some point)
  val isCollapsed = rememberSaveable { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth().requiredHeight(mapHeight.floatValue.dp)) {
      MapContent(
          modifier = Modifier.fillMaxWidth().height(mapHeight.floatValue.dp),
          travelContainers = travelList.value)
    }

    // Strap handle at the bottom to drag and resize the map
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .graphicsLayer {
                  shape =
                      CutCornerShape(
                          topStart = 0.dp, // Adjust these values for the trapezoidal effect
                          topEnd = 0.dp,
                          bottomStart = 16.dp,
                          bottomEnd = 16.dp)
                  clip = true
                }
                .height(latchDp)
                .background(MaterialTheme.colorScheme.tertiary)
                .draggable(
                    orientation = Orientation.Vertical,
                    state =
                        rememberDraggableState { delta ->
                          val scaledDelta =
                              delta / density.density // Adjusting by the density scale factor

                          // Calculate the new map height based on the delta drag amount
                          if (mapHeight.floatValue + scaledDelta > maxHeightPx) {
                            mapHeight.floatValue = maxHeightPx
                          } else if (mapHeight.floatValue + scaledDelta < minHeightPx &&
                              !isCollapsed.value) {
                            isCollapsed.value = true
                            mapHeight.floatValue = 0f
                          } else {
                            if (isCollapsed.value &&
                                mapHeight.floatValue + scaledDelta > minHeightPx) {
                              isCollapsed.value = false
                            }
                            // clamp the to the min value to avoid
                            if (mapHeight.floatValue + scaledDelta < 0) {
                              mapHeight.floatValue = 0f
                            } else {
                              mapHeight.value += scaledDelta
                            }
                          }
                        })) {
          Icon(
              imageVector = Icons.Default.StopCircle,
              contentDescription = "MapLatch",
              modifier = Modifier.align(Alignment.Center))
        }
  }
}
