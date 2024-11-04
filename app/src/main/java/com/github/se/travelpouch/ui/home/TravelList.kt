package com.github.se.travelpouch.ui.home

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.TravelContainer
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
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TravelListScreen(
    navigationActions: NavigationActions,
    listTravelViewModel: ListTravelViewModel
) {
  // Fetch travels when the screen is launched
  LaunchedEffect(Unit) {
    listTravelViewModel.getTravels()
    // sleep the thread for 1 second to allow the data to be fetched
  }
  // travelContainers.getTravels()
  val travelList = listTravelViewModel.travels.collectAsState().value

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
            tabList = listOf(TopLevelDestinations.TRAVELS, TopLevelDestinations.CALENDAR),
            navigationActions = navigationActions)
      },
      content = { pd ->
        Column {
          // Add the map to display the travels
          MapContent(
              modifier = Modifier.fillMaxWidth().height(300.dp), travelContainers = travelList)

          if (travelList.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(pd)) {
                  items(travelList.size) { index ->
                    TravelItem(travelContainer = travelList[index]) {
                      listTravelViewModel.selectTravel(travelList[index])
                      navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
                    }
                  }
                }
          } else {
            Text(
                modifier = Modifier.padding(pd).testTag("emptyTravelPrompt"),
                text = "You have no travels yet.")
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
