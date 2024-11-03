package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.ui.navigation.BottomNavigationMenu
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.ui.navigation.TopLevelDestinations
import java.util.Calendar
import java.util.GregorianCalendar

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
            tabList = listOf(TopLevelDestinations.ACTIVITIES, TopLevelDestinations.MAP),
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
            modifier = Modifier.fillMaxSize().padding(pd) // Apply scaffold padding to the whole box
            ) {
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
                            })
                      }
                    }
                  }

              if (showBanner.value) {
                NextActivitiesBanner(
                    activities = listOfActivities.value,
                    onDismiss = { showBanner.value = false },
                )
              }
            }
      }
}

/**
 * This function displays the date of an activity, its title and its location in a card.
 *
 * @param activity (Activity) : the activity to display
 * @param onClick (() -> Unit) : the function to apply when we click on an activity.
 */
@Composable
fun ActivityItem(activity: Activity, onClick: () -> Unit = {}) {
  val calendar = GregorianCalendar()
  calendar.time = activity.date.toDate()

  Card(modifier = Modifier.testTag("activityItem").clickable(onClick = onClick).fillMaxSize()) {
    Text(activity.title)
    Text(activity.location.name)
    Text(
        "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                calendar.get(
                    Calendar.YEAR
                )
            }")
  }
}
