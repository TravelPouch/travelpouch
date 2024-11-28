package com.github.se.travelpouch.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.dashboard.CalendarViewModel
import com.github.se.travelpouch.model.documents.DocumentViewModel
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.ui.dashboard.CalendarScreen
import com.github.se.travelpouch.ui.dashboard.TravelActivitiesScreen
import com.github.se.travelpouch.ui.documents.DocumentListScreen

// credit to : https://www.youtube.com/watch?v=inrGyNJbZaI&t=620s

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipePager(
    navigationActions: NavigationActions,
    activityViewModel: ActivityViewModel,
    calendarViewModel: CalendarViewModel,
    documentViewModel: DocumentViewModel,
    listTravelViewModel: ListTravelViewModel,
    onNavigateToDocumentPreview: () -> Unit
) {
  val listOfTopLevelDestinationForSwipe =
      listOf(
          TopLevelDestinations.ACTIVITIES,
          TopLevelDestinations.CALENDAR,
          TopLevelDestinations.DOCUMENTS)

  val listOfTopLevelDestination =
      listOf(
          TopLevelDestinations.ACTIVITIES,
          TopLevelDestinations.CALENDAR,
          TopLevelDestinations.DOCUMENTS,
          TopLevelDestinations.MAP)

  val pagerState = rememberPagerState(initialPage = 0) { listOfTopLevelDestinationForSwipe.size }

  var selectedScreen by remember { mutableIntStateOf(pagerState.currentPage) }

  LaunchedEffect(selectedScreen) { pagerState.scrollToPage(selectedScreen) }

  LaunchedEffect(pagerState.currentPage) { selectedScreen = pagerState.currentPage }

  Scaffold(
      modifier = Modifier.testTag("pagerSwipe"),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag("topBar"),
            title = {
              Text(
                  listOfTopLevelDestinationForSwipe[selectedScreen].textId,
                  Modifier.testTag("travelTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    when (pagerState.currentPage) {
                      0 -> navigationActions.navigateTo(Screen.TRAVEL_LIST)
                      1 -> selectedScreen = 0
                      2 -> selectedScreen = 0
                    }
                  },
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
            })
      },
      bottomBar = {
        NavigationBar(modifier = Modifier.testTag("navigationBarTravelList")) {
          listOfTopLevelDestination.forEach { destination ->
            NavigationBarItem(
                onClick = {
                  when (destination.textId) {
                    "Activities" -> selectedScreen = 0
                    "Calendar" -> selectedScreen = 1
                    "Documents" -> selectedScreen = 2
                    "Map" -> navigationActions.navigateTo(Screen.ACTIVITIES_MAP)
                  }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                selected = false,
                label = { Text(destination.textId) },
                modifier = Modifier.testTag(destination.textId))
          }
        }
      }) { pd ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(pd)) { pageIndex ->
          when (pageIndex) {
            0 -> TravelActivitiesScreen(navigationActions, activityViewModel)
            1 -> CalendarScreen(calendarViewModel, navigationActions)
            2 ->
                DocumentListScreen(
                    documentViewModel,
                    listTravelViewModel,
                    navigationActions,
                    onNavigateToDocumentPreview)
          }
        }
      }
}
