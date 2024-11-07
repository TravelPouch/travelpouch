package com.github.se.travelpouch.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations
import java.util.Calendar
import java.util.GregorianCalendar

private const val A4_ASPECT_RATIO = 1f / 1.414f

/**
 * This function describes how the list of activities of the travel is displayed.
 *
 * @param navigationActions (NavigationActions?) : the navigation actions that describes how to go
 *   from a screen to another.
 * @param activityModelView (ActivityModelView) : the model view for an activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelActivitiesScreen(
    navigationActions: NavigationActions,
    activityModelView: ActivityViewModel
) {

  activityModelView.getAllActivities()

  val showBanner = remember { mutableStateOf(true) }
  val listOfActivities = activityModelView.activities.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("travelActivitiesScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Travel", Modifier.testTag("travelTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_LIST) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.EDIT_TRAVEL_SETTINGS) },
                  modifier = Modifier.testTag("settingsButton")) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                  }

              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.TIMELINE) },
                  modifier = Modifier.testTag("eventTimelineButton")) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                  }

              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.DOCUMENT_LIST) },
                  modifier = Modifier.testTag("documentListButton")) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = null)
                  }

              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.PROFILE) },
                  modifier = Modifier.testTag("ProfileButton")) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            tabList =
                listOf(
                    TopLevelDestinations.ACTIVITIES,
                    TopLevelDestinations.CALENDAR,
                    TopLevelDestinations.MAP),
            navigationActions = navigationActions)
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationActions.navigateTo(Screen.ADD_ACTIVITY) },
            modifier = Modifier.testTag("addActivityButton")) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
      }) { pd ->
        Box(
            modifier =
                Modifier.fillMaxSize().padding(pd), // Apply scaffold padding to the whole box
            contentAlignment = Alignment.Center) {
              LazyColumn(
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  contentPadding = PaddingValues(vertical = 8.dp),
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(horizontal = 16.dp)
                          .testTag("activityColumn")) {
                    if (listOfActivities.value.isEmpty()) {
                      item {
                        Text(
                            text = "No activities planned for this trip",
                            modifier = Modifier.testTag("emptyTravel"))
                      }
                    } else {
                      items(listOfActivities.value.size) { idx ->
                        ActivityItem(
                            listOfActivities.value[idx],
                            onClick = {
                              activityModelView.selectActivity(listOfActivities.value[idx])
                              navigationActions.navigateTo(Screen.EDIT_ACTIVITY)
                            },
                            LocalContext.current)
                      }
                    }
                  }

              if (showBanner.value) {
                NextActivitiesBanner(
                    activities = listOfActivities.value,
                    onDismiss = { showBanner.value = false },
                    localConfig = LocalConfiguration.current)
              }
            }
      }
}

/**
 * This function displays the date of an activity, its title, its location and first 2 images.
 *
 * @param activity (Activity) : the activity to display
 * @param onClick (() -> Unit) : the function to apply when we click on an activity.
 */
@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit = {}, context: android.content.Context) {
  val calendar = GregorianCalendar().apply { time = activity.date.toDate() }
  val images =
      listOf(
          "https://img.yumpu.com/30185842/1/500x640/afps-attestation-de-formation-aux-premiers-secours-programme-.jpg",
          "https://wallpapercrafter.com/desktop6/1606440-architecture-buildings-city-downtown-finance-financial.jpg",
          "https://assets.entrepreneur.com/content/3x2/2000/20151023204134-poker-game-gambling-gamble-cards-money-chips-game.jpeg")
  Card(
      modifier = Modifier.testTag("activityItem").fillMaxSize(),
      onClick = onClick,
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      border = BorderStroke(1.dp, Color.DarkGray)) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text(activity.title)
          Text(activity.location.name)
          Text(
              "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")

          // Handling image display logic
          if (images.isEmpty()) {
            // No images to show, do nothing
          } else if (images.size == 1) {
            // Single image - crop to A4 aspect ratio
            Box(
                modifier =
                    Modifier.fillMaxWidth() // Fill the width of the parent
                        .aspectRatio(
                            A4_ASPECT_RATIO) // Maintain A4 aspect ratio (width / height ~ 1:1.414)
                        .background(Color.Transparent)) {
                  AdvancedImageDisplayWithEffects(
                      imageUrl = images[0],
                      loadingContent = { DefaultLoadingUI() },
                      errorContent = { DefaultErrorUI() })
                }
          } else if (images.size == 2) {
            // Two images - display side by side with a vertical separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                  images.take(2).forEach { imageUrl ->
                    Box(
                        modifier =
                            Modifier.weight(1f)
                                .aspectRatio(
                                    A4_ASPECT_RATIO) // Use the same A4 aspect ratio for both images
                                .background(Color.Transparent)) {
                          AdvancedImageDisplayWithEffects(
                              imageUrl = imageUrl,
                              loadingContent = { DefaultLoadingUI() },
                              errorContent = { DefaultErrorUI() })
                        }
                  }
                }
          } else if (images.size >= 3) {
            // Three or more images - show the first two with a button above them
            Column(modifier = Modifier.fillMaxWidth()) {
              Box { // Use Box to layer the images and the button
                // Display the first two images inside a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                      images.take(2).forEach { imageUrl ->
                        Box(
                            modifier =
                                Modifier.weight(1f) // Ensure the images take equal space
                                    .aspectRatio(A4_ASPECT_RATIO) // Maintain A4 aspect ratio
                                    .background(Color.Transparent)) {
                              AdvancedImageDisplayWithEffects(
                                  imageUrl = imageUrl,
                                  loadingContent = { DefaultLoadingUI() },
                                  errorContent = { DefaultErrorUI() })
                            }
                      }
                    }

                // More options button on top of the second image
                IconButton(
                    onClick = {
                      Toast.makeText(
                              context,
                              "Placeholder for document view of activity",
                              Toast.LENGTH_LONG)
                          .show()
                    },
                    modifier =
                        Modifier.align(Alignment.BottomEnd) // Position the button on the top right
                            .background(
                                Color.Gray,
                                shape =
                                    androidx.compose.foundation.shape.RoundedCornerShape(
                                        10)) // Rounded background
                    ) {
                      Icon(
                          imageVector = Icons.Default.MoreVert,
                          contentDescription = "More options",
                          tint = Color.White)
                    }
              }
            }
          }
        }
      }
}

// Large parts of this code was taken without shame from
// https://medium.com/@ramadan123sayed/comprehensive-guide-to-utilizing-icons-and-images-in-jetpack-compose-with-coil-7fd2686e491e
// This is good stuff

@Composable
fun AdvancedImageDisplayWithEffects(
    imageUrl: String,
    loadingContent: @Composable () -> Unit = { DefaultLoadingUI() },
    errorContent: @Composable () -> Unit = { DefaultErrorUI() }
) {
  SubcomposeAsyncImage(
      model = ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build(),
      contentDescription = "Advanced Image with Effects",
      modifier = Modifier.fillMaxWidth(),
      contentScale = ContentScale.Fit,
      loading = { loadingContent() },
      error = { errorContent() })
}

@Composable
fun DefaultLoadingUI() {
  Box(
      modifier = Modifier.fillMaxSize().background(Color.LightGray),
      contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
}

@Composable
fun DefaultErrorUI() {
  Box(
      modifier = Modifier.fillMaxSize().background(Color.Gray),
      contentAlignment = Alignment.Center) {
        Text("Failed to load image", color = Color.Red)
      }
}
