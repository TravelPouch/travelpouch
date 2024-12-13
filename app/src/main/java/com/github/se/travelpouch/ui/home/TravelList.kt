package com.github.se.travelpouch.ui.home

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations
import com.github.se.travelpouch.ui.theme.logoutIconDark
import com.github.se.travelpouch.ui.theme.logoutIconLight
import com.github.se.travelpouch.ui.theme.logoutRedDark
import com.github.se.travelpouch.ui.theme.logoutRedLight
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Locale
import kotlinx.coroutines.launch

// ChatGpt was used to organised the floating actions buttons and for the logic of tapping outside
// the drawer menu to close it, while keeping the gestures of the drawer menu disabled
// https://www.youtube.com/watch?v=aYSarwALlpI&t=307s was used to organised the
// ModalNavigationDrawer

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

  val listOfTopLevelDestinations =
      listOf(
          TopLevelDestinations.TRAVELS,
          TopLevelDestinations.NOTIFICATION,
          TopLevelDestinations.PROFILE,
          TopLevelDestinations.STORAGE)

  val travelList = listTravelViewModel.travels.collectAsState()
  val currentProfile = profileModelView.profile.collectAsState()
  val isLoading = listTravelViewModel.isLoading.collectAsState()

  // Used for the screen orientation redraw
  val mapPlusLatchHeight = 300.dp

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val configuration = LocalConfiguration.current
  val darkTheme = isSystemInDarkTheme()
  ModalNavigationDrawer(
      drawerContent = {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .testTag("closingMenuBox")
                    .padding(start = (configuration.screenWidthDp * 0.75).dp)
                    .clickable {
                      if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                      }
                    }) {
              FloatingActionButton(
                  onClick = { scope.launch { drawerState.close() } },
                  modifier =
                      Modifier.testTag("closingMenuFab")
                          .align(Alignment.TopEnd)
                          .zIndex(1f)
                          .padding(end = 8.dp)
                          .padding(top = 8.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "MenuClosing")
                  }
            }
        ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f).testTag("drawerSheetMenu")) {
          listOfTopLevelDestinations.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.textId) },
                selected = false,
                onClick = {
                  scope.launch { drawerState.close() }
                  when (item.textId) {
                    TopLevelDestinations.PROFILE.textId ->
                        navigationActions.navigateTo(Screen.PROFILE)
                    TopLevelDestinations.NOTIFICATION.textId ->
                        navigationActions.navigateTo(Screen.NOTIFICATION)
                    TopLevelDestinations.TRAVELS.textId -> scope.launch { drawerState.close() }
                    TopLevelDestinations.STORAGE.textId ->
                        navigationActions.navigateTo(Screen.STORAGE)
                  }
                },
                modifier =
                    Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("item${item.textId}"))
          }

          // logout button
          val tint = if (darkTheme) logoutIconDark else logoutIconLight
          val textColor = if (darkTheme) logoutRedDark else logoutRedLight

          NavigationDrawerItem(
              icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "logout icon",
                    tint = tint)
              },
              label = { Text("Log out", color = textColor) },
              selected = false,
              onClick = {
                scope.launch { drawerState.close() }
                Firebase.auth.signOut() // actual sign out
                navigationActions.navigateTo(Screen.AUTH)
              },
              modifier =
                  Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("itemLogout"))
        }
      },
      drawerState = drawerState,
      gesturesEnabled = false) {
        Box(modifier = Modifier.fillMaxSize()) {
          FloatingActionButton(
              onClick = { scope.launch { drawerState.open() } },
              modifier =
                  Modifier.testTag("menuFab")
                      .align(Alignment.TopStart)
                      .zIndex(1f)
                      .padding(start = 8.dp)
                      .padding(top = 8.dp)) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
              }

          Scaffold(
              modifier = Modifier.testTag("TravelListScreen"),
              floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigationActions.navigateTo(Screen.ADD_TRAVEL) },
                    modifier = Modifier.testTag("createTravelFab")) {
                      Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    }
              },
              content = { pd ->
                Column(modifier = Modifier.fillMaxSize().padding(pd)) {
                  // Map placed outside the LazyColumn to prevent it from being part of the
                  // scrollable
                  ResizableStowableMapWithGoogleMap(mapPlusLatchHeight, travelList) {
                      travelContainer ->
                    selectAndNavigateToTravel(
                        travelContainer,
                        listTravelViewModel,
                        navigationActions,
                        eventViewModel,
                        activityViewModel,
                        documentViewModel)
                  }

                  LazyColumn(
                      modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                      contentPadding = PaddingValues(bottom = 80.dp)) {
                        if (travelList.value.isNotEmpty()) {
                          items(travelList.value.size) { index ->
                            TravelItem(
                                travelContainer = travelList.value[index],
                                onClick = {
                                  selectAndNavigateToTravel(
                                      travelList.value[index],
                                      listTravelViewModel,
                                      navigationActions,
                                      eventViewModel,
                                      activityViewModel,
                                      documentViewModel)
                                })
                          }
                        } else {
                          item {
                            Column(
                                modifier =
                                    Modifier.fillParentMaxSize()
                                        .padding(top = 32.dp, start = 0.dp, end = 0.dp)
                                        .padding(pd),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
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
                                      text = "Create your first travel!",
                                      style =
                                          MaterialTheme.typography.headlineMedium.copy(
                                              fontWeight = FontWeight.Bold),
                                      color = MaterialTheme.colorScheme.onBackground)
                                }
                            // Indicator to create a travel for new users
                            Box(
                                modifier =
                                    Modifier.fillMaxSize()
                                        .padding(bottom = 16.dp, end = 16.dp)
                                        .offset(
                                            y = (-60).dp), // Adjust padding to avoid overlap with
                                // FAB
                                contentAlignment = Alignment.BottomEnd // Align to the bottom right
                                ) {
                                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Create New Travel",
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Indicator Arrow",
                                        modifier =
                                            Modifier.size(40.dp)
                                                .graphicsLayer(
                                                    rotationZ = 90f) // Rotate arrow to point down
                                                .offset(
                                                    y = (-30).dp) // Adjust position to align with
                                        // FAB
                                        )
                                  }
                                }
                          }
                        }
                      }
                }
              })
        }
      }
}

/**
 * Selects a travel container and navigates to the SWIPER screen.
 *
 * @param travelContainer The travel container to select.
 * @param listTravelViewModel The view model for the list of travels.
 * @param navigationActions The actions for navigation.
 * @param eventViewModel The view model for events.
 * @param activityViewModel The view model for activities.
 * @param documentViewModel The view model for documents.
 * @return A lambda function that performs the selection and navigation.
 */
fun selectAndNavigateToTravel(
    travelContainer: TravelContainer,
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions,
    eventViewModel: EventViewModel,
    activityViewModel: ActivityViewModel,
    documentViewModel: DocumentViewModel
) {
  val travelId = travelContainer.fsUid
  listTravelViewModel.selectTravel(travelContainer)
  navigationActions.navigateTo(Screen.SWIPER)
  eventViewModel.setIdTravel(travelId)
  activityViewModel.setIdTravel(travelId)
  documentViewModel.setIdTravel(travelId)
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
                    text = travelContainer.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      text =
                          SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                              .format(travelContainer.startTime.toDate()),
                      style = MaterialTheme.typography.bodySmall)
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null)
                }
              }

          Spacer(modifier = Modifier.height(4.dp))

          // Description
          Text(
              text = travelContainer.description,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Normal)

          // Location Name
          Text(
              text = travelContainer.location.name,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.ExtraLight)
        }
      }
}

// inspired from :
// https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling

/**
 * Composable function that displays a resizable and stowable map with Google Map integration.
 *
 * @param maxMapPlusLatchDp The maximum height of the map in Dp. Default is 300.dp.
 * @param travelList The list of travel containers to be displayed on the map.
 * @param onInfoWindowClickCallback Callback function to be invoked when a marker info window is
 *   clicked.
 */
@Composable
fun ResizableStowableMapWithGoogleMap(
    maxMapPlusLatchDp: Dp = 300.dp,
    travelList: State<List<TravelContainer>>,
    onInfoWindowClickCallback: (TravelContainer) -> Unit = {},
) {

  // State to track the height of the map
  val minHeight = 100f // Min height of the map (collapsed state)
  val latchDp = 30.dp // Height of the strap handle
  val maxHeight = maxMapPlusLatchDp.value - latchDp.value // Max height of the map
  val mapHeight = rememberSaveable {
    mutableFloatStateOf(maxHeight)
  } // Initial height of the map in pixels
  val density = LocalDensity.current // Get the density scale factor

  // Track whether the map is collapsed (height passed 0 at some point)
  val belowThreshold = rememberSaveable { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth().requiredHeight(mapHeight.floatValue.dp)) {
      MapContent(
          modifier = Modifier.fillMaxWidth().height(mapHeight.floatValue.dp),
          travelContainers = travelList.value,
          onInfoWindowClickCallback = { travelContainer ->
            // Handle the click event
            onInfoWindowClickCallback(travelContainer)
          })
    }

    // Strap handle at the bottom to drag and resize the map
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .testTag("mapLatch")
                .graphicsLayer {
                  shape =
                      CutCornerShape(
                          topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                  clip = true
                }
                .height(latchDp)
                .background(MaterialTheme.colorScheme.errorContainer)
                .draggable(
                    orientation = Orientation.Vertical,
                    state =
                        rememberDraggableState { delta ->
                          resizeFromDragMotion(
                              delta, density, mapHeight, maxHeight, minHeight, belowThreshold)
                        })) {
          Icon(
              imageVector = Icons.Default.Adjust,
              contentDescription = "MapLatch",
              modifier = Modifier.align(Alignment.Center))
        }
  }
}

/**
 * Adjusts the height of the map based on the drag motion.
 *
 * @param delta The change in position from the drag motion.
 * @param density The density scale factor.
 * @param mapHeight The current height of the map.
 * @param maxHeight The maximum height of the map in pixels.
 * @param minHeight The minimum height of the map in pixels.
 * @param belowThreshold A state indicating whether the map is collapsed.
 */
private fun resizeFromDragMotion(
    delta: Float,
    density: Density,
    mapHeight: MutableFloatState,
    maxHeight: Float,
    minHeight: Float,
    belowThreshold: MutableState<Boolean>
) {
  val scaledDelta = delta / density.density // Adjusting by the density scale factor

  // Calculate the new map height based on the delta drag amount
  // clamp to the max value to avoid exceeding the max height
  if (mapHeight.floatValue + scaledDelta > maxHeight) {
    mapHeight.floatValue = maxHeight
  } else if (mapHeight.floatValue + scaledDelta < minHeight && !belowThreshold.value) {
    belowThreshold.value = true
    mapHeight.floatValue = 0f
  } else {
    if (belowThreshold.value && mapHeight.floatValue + scaledDelta > minHeight) {
      belowThreshold.value = false
    }
    // clamp to the min value to avoid negatives values
    if (mapHeight.floatValue + scaledDelta < 0) {
      mapHeight.floatValue = 0f
    } else {
      mapHeight.floatValue += scaledDelta
    }
  }
}
