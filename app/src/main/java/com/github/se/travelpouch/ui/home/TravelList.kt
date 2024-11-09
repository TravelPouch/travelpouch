package com.github.se.travelpouch.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import java.util.Locale

/**
 * Composable function for the travels list screen.
 *
 * @param navigationActions Actions for navigation.
 * @param listTravelViewModel List of travels as a viewmodel, to display.
 */
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
    // sleep the thread for 1 second to allow the data to be fetched
  }
  val travelList = listTravelViewModel.travels.collectAsState()
  val currentProfile = profileModelView.profile.collectAsState()

  // Used for the screen orientation redraw
  val configuration = LocalConfiguration.current
  val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
  val mapHeight = if (isPortrait) 300.dp else 200.dp

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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pd),
            contentPadding = PaddingValues(bottom = 80.dp)) {
              item {
                MapContent(
                    modifier = Modifier.fillMaxWidth().height(mapHeight),
                    travelContainers = travelList.value)
              }
              if (travelList.value.isNotEmpty()) {
                items(travelList.value.size) { index ->
                  TravelItem(travelContainer = travelList.value[index]) {
                    listTravelViewModel.selectTravel(travelList.value[index])
                    navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
                  }
                }
              } else {
                item {
                  Box(
                      modifier = Modifier.fillMaxSize().padding(16.dp),
                      contentAlignment = Alignment.Center) {
                        Text(
                            modifier = Modifier.testTag("emptyTravelPrompt"),
                            text = "You have no travels yet.")
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
              .padding(vertical = 4.dp)
              .clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
      // Date and Title Row
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Description
      Text(text = travelContainer.description, style = MaterialTheme.typography.bodyMedium)

      // Location Name
      Text(
          text = travelContainer.location.name,
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray)
    }
  }
}
